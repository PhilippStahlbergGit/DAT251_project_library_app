
from typing import List
from pydantic import BaseModel

try:
    from .models import Book
except ImportError:
    from models import Book

try:
    from .Recommendation_system import ensure_recommender, recommend_from_books
except ImportError:
    from Recommendation_system import ensure_recommender, recommend_from_books
import pandas as pd
import json
import csv
import io
import zipfile

import re


import os
from pathlib import Path

class Recommandation(BaseModel):
    book: Book
    score: float


def _normalize_text(value: str | None) -> str:
    if not value:
        return ""
    normalized = re.sub(r"[^a-z0-9]+", " ", str(value).casefold())
    return " ".join(normalized.split())


def _match_owned_book_row(catalog_df: pd.DataFrame, owned_book: Book) -> pd.DataFrame:
    title_raw = (owned_book.title or "").strip()
    if not title_raw:
        return catalog_df.iloc[0:0]

    title_series = catalog_df["Name"].astype(str)

    # 1) Exact (case-sensitive), fast path.
    exact = catalog_df[title_series == title_raw]
    if not exact.empty:
        return exact.iloc[[0]]

    # 2) Exact (case-insensitive).
    title_casefold = title_raw.casefold()
    exact_ci = catalog_df[title_series.str.casefold() == title_casefold]
    if not exact_ci.empty:
        return exact_ci.iloc[[0]]

    # 3) Normalized exact (remove punctuation, collapse spaces, casefold).
    normalized_catalog_titles = title_series.map(_normalize_text)
    normalized_title = _normalize_text(title_raw)
    normalized_exact = catalog_df[normalized_catalog_titles == normalized_title]
    if not normalized_exact.empty:
        return normalized_exact.iloc[[0]]

    # 4) Substring fallback for cases like missing subtitle/suffix in input title.
    if normalized_title:
        contains = catalog_df[
            normalized_catalog_titles.str.contains(re.escape(normalized_title), na=False)
        ]
        if not contains.empty:
            # Optional lightweight author preference.
            if owned_book.authors:
                wanted_authors = {_normalize_text(a) for a in owned_book.authors if a}
                contains_authors = contains[contains["Authors"].astype(str).map(_normalize_text).isin(wanted_authors)]
                if not contains_authors.empty:
                    return contains_authors.iloc[[0]]
            return contains.iloc[[0]]

    return catalog_df.iloc[0:0]


def _load_local_env_file() -> None:
    env_path = Path(__file__).with_name(".env")
    if not env_path.exists():
        return

    raw_bytes = env_path.read_bytes()
    env_text = None
    for encoding in ("utf-8", "utf-8-sig", "cp1252", "latin1"):
        try:
            env_text = raw_bytes.decode(encoding)
            break
        except UnicodeDecodeError:
            continue

    if env_text is None:
        raise ValueError(f"Could not decode env file: {env_path}")

    for raw_line in env_text.splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue

        key, value = line.split("=", 1)
        key = key.strip()
        value = value.strip()

        if (
            len(value) >= 2
            and value[0] == value[-1]
            and value[0] in ('"', "'")
        ):
            value = value[1:-1]

        # Keep real environment variables as the highest priority.
        os.environ.setdefault(key, value)


_load_local_env_file()


def _parse_files_env(value: str | None) -> List[str]:
    # for reusability later, to add more files if every needed.
    if not value:
        return []

    # Supports JSON list syntax: FILES='["a.csv", "b.csv"]'
    try:
        parsed = json.loads(value)
        if isinstance(parsed, list):
            return [str(v).strip() for v in parsed if str(v).strip()]
    except json.JSONDecodeError:
        pass

    # Supports comma-separated syntax: FILES='a.csv,b.csv'
    return [part.strip() for part in value.split(",") if part.strip()]


def _get_data_root_and_files() -> tuple[Path, List[str]]:
    data_root_raw = os.getenv("DATA_ROOT", "").strip().strip('"').strip("'")
    data_root = Path(data_root_raw) if data_root_raw else Path(__file__).with_name("data")
    files = _parse_files_env(os.getenv("FILES"))
    return data_root, files




def _load_dataset_file(data_root: Path, file_name: str) -> pd.DataFrame:
    required_cols = ["Id", "Name", "Authors", "pagesNumber", "PublishYear", "Rating", "RatingDistTotal"]

    def _canon(col_name: str) -> str:
        return "".join(ch for ch in str(col_name).lower() if ch.isalnum())

    expected_by_canon = {
        _canon("Id"): "Id",
        _canon("Name"): "Name",
        _canon("Authors"): "Authors",
        _canon("pagesNumber"): "pagesNumber",
        _canon("PublishYear"): "PublishYear",
        _canon("Rating"): "Rating",
        _canon("RatingDistTotal"): "RatingDistTotal",
    }

    def _read_csv_with_fallbacks(source, source_label: str) -> pd.DataFrame:
        last_exc = None
        for encoding in ("utf-8", "utf-8-sig", "cp1252", "latin1"):
            read_strategies = [
                {"encoding": encoding, "sep": None, "engine": "python"},
                {
                    "encoding": encoding,
                    "sep": None,
                    "engine": "python",
                    "on_bad_lines": "skip",
                },
                {
                    "encoding": encoding,
                    "sep": ",",
                    "engine": "python",
                    "quoting": csv.QUOTE_NONE,
                    "on_bad_lines": "skip",
                    "escapechar": "\\",
                },
            ]

            for pandas_kwargs in read_strategies:
                try:
                    if isinstance(source, (bytes, bytearray)):
                        df = pd.read_csv(io.BytesIO(source), **pandas_kwargs)
                    else:
                        df = pd.read_csv(source, **pandas_kwargs)
                except Exception as exc:
                    last_exc = exc
                    continue

                rename_map = {}
                for original_col in df.columns:
                    canonical = _canon(str(original_col).replace("\ufeff", "").strip())
                    if canonical in expected_by_canon:
                        rename_map[original_col] = expected_by_canon[canonical]

                if rename_map:
                    df = df.rename(columns=rename_map)

                missing = [col for col in required_cols if col not in df.columns]
                if missing:
                    last_exc = ValueError(
                        f"Missing expected columns {missing} in '{source_label}'. "
                        f"Detected columns: {list(df.columns)}"
                    )
                    continue

                return df[required_cols]

        raise ValueError(
            f"Could not parse dataset file '{source_label}'. Last error: {last_exc}"
        )

    direct_matches = list(data_root.rglob(file_name))
    if direct_matches:
        matched_path = direct_matches[0]
        if matched_path.suffix.lower() == ".zip":
            with zipfile.ZipFile(matched_path, "r") as zf:
                member = next((n for n in zf.namelist() if n.endswith(file_name)), None)
                if member is None:
                    member = next((n for n in zf.namelist() if n.lower().endswith(".csv")), None)
                if member is None:
                    raise ValueError(f"Zip file '{matched_path.name}' contains no CSV files")
                return _read_csv_with_fallbacks(zf.read(member), f"{matched_path.name}:{member}")
        return _read_csv_with_fallbacks(matched_path, matched_path.name)

    zip_candidates = list(data_root.rglob("*.zip"))
    for zip_path in zip_candidates:
        with zipfile.ZipFile(zip_path, "r") as zf:
            member = next((n for n in zf.namelist() if n.endswith(file_name)), None)
            if member is not None:
                return _read_csv_with_fallbacks(zf.read(member), f"{zip_path.name}:{member}")

    raise ValueError(f"Could not find '{file_name}' under local data root '{data_root}'")




def getData():
    data_root, files = _get_data_root_and_files()

    if not files:
        raise ValueError(
            "Missing/invalid FILES env var. Use comma-separated values or a JSON list, e.g. FILES='books.csv,ratings.csv'"
        )
    if not data_root.exists():
        raise ValueError(
            f"Data root does not exist: '{data_root}'. Set DATA_ROOT or add files under recommendation_pipeline/data."
        )

    dfs = [_load_dataset_file(data_root, f) for f in files]

    df = pd.concat(dfs, ignore_index=True)
    return df



def make_recommendations(owned_books: List[Book] | Book, k: int, df: pd.DataFrame) -> List[Recommandation]:
    """
    Make recommendations based on user-owned books.
    Parameters:
        owned_books: List of Book objects or a single Book
        k: number of recommendations
        df: the dataset DataFrame to use for recommendations
    Returns:
        List of Recommandation objects
    """
    if isinstance(owned_books, Book):
        owned_books = [owned_books]

    if not owned_books:
        return []

    catalog_df = ensure_recommender(df)

    # Convert Book models into single-row DataFrames
    book_dfs = []
    for book in owned_books:
        match = _match_owned_book_row(catalog_df, book)
        if match.empty:
            continue
        book_dfs.append(match.iloc[[0]])

    if not book_dfs:
        # None of the user's books were found in the dataset
        return []

    # Call the recommendation engine
    raw_results = recommend_from_books(book_dfs, k)

    recommendations: List[Recommandation] = []

    for row, score in raw_results:
        book_model = Book(
            id=int(row["Id"]),
            title=row["Name"],
            authors=[row["Authors"]] if "Authors" in row else None,
            publicationYear=int(row["PublishYear"]) if "PublishYear" in row else None,
        )
        recommendations.append(Recommandation(book=book_model, score=score))

    return recommendations

#Should return like this: class Recommandation(BaseModel):
    #book: Book
    #score: float




