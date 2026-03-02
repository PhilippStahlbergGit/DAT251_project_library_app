# ==============================
# INSTALL & IMPORTS
# ==============================

# import kagglehub
# from kagglehub import KaggleDatasetAdapter  # Kept commented for future use

import numpy as np
import pandas as pd
import faiss
from sklearn.preprocessing import RobustScaler
from sklearn.feature_extraction.text import TfidfVectorizer, FeatureHasher
from sklearn.decomposition import TruncatedSVD

# ==============================
# LOAD DATA
# ==============================
# You can still use kagglehub later if needed
# DATASET = "bahramjannesarr/goodreads-book-datasets-10m"
FILES = ["book1-100k.csv", "book1000k-1100k.csv"]

# Load CSVs locally
dfs = [pd.read_csv(f, usecols=["Id","Name","Authors","pagesNumber","PublishYear","Rating","RatingDistTotal"]) for f in FILES]

# Uncomment the kagglehub version if you want to fetch directly from Kaggle
# dfs = [
#     kagglehub.load_dataset(
#         KaggleDatasetAdapter.PANDAS,
#         DATASET,
#         f,
#         pandas_kwargs={"usecols": ["Id", "Name", "Authors", "pagesNumber", "PublishYear", "Rating", "RatingDistTotal"]}
#     )
#     for f in FILES
# ]

df = pd.concat(dfs, ignore_index=True)

# Handle missing authors
df["Authors"] = df["Authors"].fillna("Unknown Author")

# Author IDs
df["AuthorId"], unique_authors = pd.factorize(df["Authors"])

# Clean rating
df["RatingDistTotal"] = df["RatingDistTotal"].str.replace("total:", "", regex=False).astype(int)
df["RatingRatio"] = np.where(df["Rating"] == 0, 0, df["RatingDistTotal"] / df["Rating"])
df.drop(columns=["Authors", "RatingDistTotal", "Rating"], inplace=True)

# Lookup dictionaries
bookid_to_name = dict(zip(df["Id"], df["Name"]))
authorid_to_name = dict(zip(df["AuthorId"], unique_authors))

# ==============================
# FEATURE ENGINEERING
# ==============================
TITLE_WEIGHT = 0.3
AUTHOR_WEIGHT = 0.1

# Numeric features
df["ratingRatio_log"] = np.log1p(df["RatingRatio"])
num_cols = ["pagesNumber", "PublishYear", "ratingRatio_log"]
scaler = RobustScaler()
X_num = scaler.fit_transform(df[num_cols].astype(float)).astype(np.float32)

# Title features
tfidf = TfidfVectorizer(stop_words="english", max_features=5000, ngram_range=(1,2), min_df=2)
X_title_sparse = tfidf.fit_transform(df["Name"].fillna(""))
svd = TruncatedSVD(n_components=256, random_state=42)
X_title = svd.fit_transform(X_title_sparse).astype(np.float32) * TITLE_WEIGHT

# Author features
hasher = FeatureHasher(n_features=512, input_type="string", alternate_sign=False)
author_tokens = df["AuthorId"].astype(str).apply(lambda a: [f"author={a}"]).tolist()
X_author = hasher.transform(author_tokens).toarray().astype(np.float32) * AUTHOR_WEIGHT

# Combine
X = np.hstack([X_num, X_author, X_title]).astype(np.float32)
faiss.normalize_L2(X)

# ==============================
# BUILD FAISS INDEX
# ==============================
d = X.shape[1]
index = faiss.IndexFlatIP(d)
index.add(X)

# ==============================
# VECTORIZE SINGLE BOOK
# ==============================
def vectorize_one_book(book_df: pd.DataFrame) -> np.ndarray:
    """Vectorize a single-row DataFrame into the feature space."""
    row = book_df.iloc[0]

    # Numeric
    pages = float(row["pagesNumber"])
    year = float(row["PublishYear"])
    rr = float(row["RatingRatio"])
    rr_log = np.log1p(rr)
    X_num_vec = scaler.transform(np.array([[pages, year, rr_log]], dtype=np.float32))

    # Author
    author_id = str(int(row["AuthorId"])) if "AuthorId" in row else "0"
    X_author_vec = hasher.transform([[f"author={author_id}"]]).toarray().astype(np.float32) * AUTHOR_WEIGHT

    # Title
    X_title_vec = svd.transform(tfidf.transform([row["Name"]])).astype(np.float32) * TITLE_WEIGHT

    # Combine
    vec = np.hstack([X_num_vec, X_author_vec, X_title_vec]).astype(np.float32)
    faiss.normalize_L2(vec)
    return vec

# ==============================
# RECOMMEND FROM LIST OF BOOKS
# ==============================
def recommend_from_books(book_dfs: list, k: int = 10):
    """
    book_dfs: list of single-row DataFrames
    Returns: List[tuple[pd.Series, float]]
    """

    vectors = [vectorize_one_book(b) for b in book_dfs]
    user_vector = np.mean(np.vstack(vectors), axis=0, keepdims=True)
    faiss.normalize_L2(user_vector)

    D, I = index.search(user_vector, k + len(book_dfs))

    owned_titles = set([b.iloc[0]["Name"].lower() for b in book_dfs])

    results = []

    for row_idx, score in zip(I[0], D[0]):
        row = df.iloc[row_idx]
        title = row["Name"]

        if title.lower() in owned_titles:
            continue

        results.append((row, float(score)))

        if len(results) >= k:
            break

    return results