import numpy as np
import pandas as pd
import faiss
from sklearn.preprocessing import RobustScaler
from sklearn.feature_extraction.text import TfidfVectorizer, FeatureHasher
from sklearn.decomposition import TruncatedSVD

TITLE_WEIGHT = 0.3
AUTHOR_WEIGHT = 0.1

_catalog_df: pd.DataFrame | None = None
_source_df_id: int | None = None
_scaler: RobustScaler | None = None
_tfidf: TfidfVectorizer | None = None
_svd: TruncatedSVD | None = None
_hasher: FeatureHasher | None = None
_index: faiss.IndexFlatIP | None = None


def _prepare_catalog_df(source_df: pd.DataFrame) -> pd.DataFrame:
    required_cols = ["Id", "Name", "Authors", "pagesNumber", "PublishYear", "Rating", "RatingDistTotal"]
    missing = [c for c in required_cols if c not in source_df.columns]
    if missing:
        raise ValueError(f"Source DataFrame is missing required columns: {missing}")

    data = source_df.copy()
    data["Authors"] = data["Authors"].fillna("Unknown Author")
    data["AuthorId"], _ = pd.factorize(data["Authors"])

    rating_dist = (
        data["RatingDistTotal"]
        .astype(str)
        .str.replace("total:", "", regex=False)
        .str.replace(",", "", regex=False)
    )
    data["RatingDistTotal"] = pd.to_numeric(rating_dist, errors="coerce").fillna(0)
    data["Rating"] = pd.to_numeric(data["Rating"], errors="coerce").fillna(0)

    data["RatingRatio"] = np.where(data["Rating"] == 0, 0, data["RatingDistTotal"] / data["Rating"])
    data["ratingRatio_log"] = np.log1p(data["RatingRatio"])

    for col in ["pagesNumber", "PublishYear", "ratingRatio_log"]:
        data[col] = pd.to_numeric(data[col], errors="coerce").fillna(0.0)

    data["Name"] = data["Name"].fillna("")
    return data


def ensure_recommender(source_df: pd.DataFrame) -> pd.DataFrame:
    global _catalog_df, _source_df_id, _scaler, _tfidf, _svd, _hasher, _index

    if _catalog_df is not None and _source_df_id == id(source_df):
        return _catalog_df

    catalog_df = _prepare_catalog_df(source_df)

    num_cols = ["pagesNumber", "PublishYear", "ratingRatio_log"]
    scaler = RobustScaler()
    X_num = scaler.fit_transform(catalog_df[num_cols].astype(float)).astype(np.float32)

    tfidf = TfidfVectorizer(stop_words="english", max_features=5000, ngram_range=(1, 2), min_df=2)
    X_title_sparse = tfidf.fit_transform(catalog_df["Name"])
    if X_title_sparse.shape[1] < 2 or min(X_title_sparse.shape) <= 1:
        svd = None
        X_title = X_title_sparse.toarray().astype(np.float32) * TITLE_WEIGHT
    else:
        max_rank = min(X_title_sparse.shape[0], X_title_sparse.shape[1])
        n_components = max(1, min(256, max_rank - 1))
        svd = TruncatedSVD(n_components=n_components, random_state=42)
        X_title = svd.fit_transform(X_title_sparse).astype(np.float32) * TITLE_WEIGHT

    hasher = FeatureHasher(n_features=512, input_type="string", alternate_sign=False)
    author_tokens = catalog_df["AuthorId"].astype(str).apply(lambda a: [f"author={a}"]).tolist()
    X_author = hasher.transform(author_tokens).toarray().astype(np.float32) * AUTHOR_WEIGHT

    X = np.hstack([X_num, X_author, X_title]).astype(np.float32)
    faiss.normalize_L2(X)

    index = faiss.IndexFlatIP(X.shape[1])
    index.add(X)

    _catalog_df = catalog_df
    _source_df_id = id(source_df)
    _scaler = scaler
    _tfidf = tfidf
    _svd = svd
    _hasher = hasher
    _index = index
    return _catalog_df


def vectorize_one_book(book_df: pd.DataFrame) -> np.ndarray:
    """Vectorize a single-row DataFrame into the feature space."""
    if _scaler is None or _tfidf is None or _hasher is None:
        raise ValueError("Recommender is not initialized. Call ensure_recommender() first.")

    row = book_df.iloc[0]

    pages = float(row["pagesNumber"])
    year = float(row["PublishYear"])
    rr = float(row["RatingRatio"])
    rr_log = np.log1p(rr)
    X_num_vec = _scaler.transform(np.array([[pages, year, rr_log]], dtype=np.float32))

    author_id = str(int(row["AuthorId"])) if "AuthorId" in row else "0"
    X_author_vec = _hasher.transform([[f"author={author_id}"]]).toarray().astype(np.float32) * AUTHOR_WEIGHT

    X_title_sparse = _tfidf.transform([row["Name"]])
    if _svd is None:
        X_title_vec = X_title_sparse.toarray().astype(np.float32) * TITLE_WEIGHT
    else:
        X_title_vec = _svd.transform(X_title_sparse).astype(np.float32) * TITLE_WEIGHT

    vec = np.hstack([X_num_vec, X_author_vec, X_title_vec]).astype(np.float32)
    faiss.normalize_L2(vec)
    return vec


def recommend_from_books(book_dfs: list[pd.DataFrame], k: int = 10):
    """
    book_dfs: list of single-row DataFrames
    Returns: List[tuple[pd.Series, float]]
    """
    if _catalog_df is None or _index is None:
        raise ValueError("Recommender is not initialized. Call ensure_recommender() first.")

    vectors = [vectorize_one_book(b) for b in book_dfs]
    user_vector = np.mean(np.vstack(vectors), axis=0, keepdims=True)
    faiss.normalize_L2(user_vector)

    D, I = _index.search(user_vector, k + len(book_dfs))

    owned_titles = set([b.iloc[0]["Name"].lower() for b in book_dfs])

    results = []

    for row_idx, score in zip(I[0], D[0]):
        row = _catalog_df.iloc[row_idx]
        title = row["Name"]

        if title.lower() in owned_titles:
            continue

        results.append((row, float(score)))

        if len(results) >= k:
            break

    return results