
from typing import List
from pydantic import BaseModel

from models import Book


import kagglehub
import pandas as pd
import json
import csv
import io
import zipfile
from functools import lru_cache


import os
from pathlib import Path

class Recommandation(BaseModel):
    book: Book
    score: float


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


def _get_dataset_and_files() -> tuple[str, List[str]]:
    dataset = os.getenv("DATASET", "").strip().strip('"').strip("'")
    files = _parse_files_env(os.getenv("FILES"))
    return dataset, files




def _load_dataset_file(dataset: str, file_name: str) -> pd.DataFrame:
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

    @lru_cache(maxsize=8)
    def _dataset_dir(ds: str) -> Path:
        return Path(kagglehub.dataset_download(ds))

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

    ds_dir = _dataset_dir(dataset)
    direct_matches = list(ds_dir.rglob(file_name))
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

    zip_candidates = list(ds_dir.rglob("*.zip"))
    for zip_path in zip_candidates:
        with zipfile.ZipFile(zip_path, "r") as zf:
            member = next((n for n in zf.namelist() if n.endswith(file_name)), None)
            if member is not None:
                return _read_csv_with_fallbacks(zf.read(member), f"{zip_path.name}:{member}")

    raise ValueError(f"Could not find '{file_name}' in downloaded dataset at '{ds_dir}'")




def getData():
    dataset, files = _get_dataset_and_files()

    if not dataset:
        raise ValueError("Missing DATASET env var. Set DATASET before calling getData().")
    if not files:
        raise ValueError(
            "Missing/invalid FILES env var. Use comma-separated values or a JSON list, e.g. FILES='books.csv,ratings.csv'"
        )

    dfs = [_load_dataset_file(dataset, f) for f in files]

    df = pd.concat(dfs, ignore_index=True)
    return df




def convertIdToBookName(book_id: int) -> str:
    # TODO: implement this function later.
    return f"Book_{book_id}"

def make_recommendations(owned_books: List[Book] | Book, k: int) -> List[Recommandation]:
    # TOOD: implement the actual recommendation logic here.
    recommendations = []
    for i in range(k):
        book_name = convertIdToBookName(i + 1)
        recommendations.append(Recommandation(book=Book(title=book_name), score=0.5))
    return recommendations
