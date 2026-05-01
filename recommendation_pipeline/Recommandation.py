from __future__ import annotations

from collections import Counter
from dataclasses import dataclass, field
from datetime import datetime, timedelta, timezone
from typing import Any, Dict, List, Optional
import os
import re
import urllib.parse

import requests
from pydantic import BaseModel

try:
    from .models import Book
except ImportError:
    from models import Book


OPENLIBRARY_BASE_URL = os.getenv("OPENLIBRARY_BASE_URL", "https://openlibrary.org")
HTTP_TIMEOUT_SECONDS = float(os.getenv("OPENLIBRARY_TIMEOUT_SECONDS", "8"))
CACHE_TTL_SECONDS = int(os.getenv("OPENLIBRARY_CACHE_TTL_SECONDS", "900"))
MAX_GENRES = int(os.getenv("OPENLIBRARY_MAX_GENRES", "3"))
MAX_AUTHORS = int(os.getenv("OPENLIBRARY_MAX_AUTHORS", "5"))
SUBJECT_LIMIT = int(os.getenv("OPENLIBRARY_SUBJECT_LIMIT", "50"))
AUTHOR_WORKS_LIMIT = int(os.getenv("OPENLIBRARY_AUTHOR_WORKS_LIMIT", "100"))
MAX_RETRIES = int(os.getenv("OPENLIBRARY_MAX_RETRIES", "2"))
USER_AGENT = os.getenv("OPENLIBRARY_USER_AGENT", "LibraryApp-Recommendation/1.0 (contact: admin@example.com)")

GENRE_TO_SUBJECT = {
    "FICTION": "fiction",
    "NON_FICTION": "nonfiction",
    "SCIENCE_FICTION": "science_fiction",
    "FANTASY": "fantasy",
    "MYSTERY": "mystery",
    "BIOGRAPHY": "biography",
    "HISTORY": "history",
    "ROMANCE": "romance",
}
SUBJECT_KEYWORDS_TO_GENRE = {
    "science fiction": "SCIENCE_FICTION",
    "sci fi": "SCIENCE_FICTION",
    "fantasy": "FANTASY",
    "mystery": "MYSTERY",
    "romance": "ROMANCE",
    "biography": "BIOGRAPHY",
    "history": "HISTORY",
    "non fiction": "NON_FICTION",
    "nonfiction": "NON_FICTION",
    "fiction": "FICTION",
}


class Recommandation(BaseModel):
    book: Book
    score: float


@dataclass
class UserProfile:
    genre_counter: Counter[str]
    author_counter: Counter[str]
    owned_keys: set[str]
    owned_author_set: set[str]


@dataclass
class Candidate:
    key: str
    title: str
    authors: List[str]
    publication_year: Optional[int]
    edition_count: int
    source_genres: set[str] = field(default_factory=set)
    source_authors: set[str] = field(default_factory=set)


@dataclass
class CacheEntry:
    value: Any
    expires_at: datetime

    @property
    def is_expired(self) -> bool:
        return datetime.now(timezone.utc) >= self.expires_at


_CACHE: Dict[str, CacheEntry] = {}
_SESSION = requests.Session()
_SESSION.headers.update({"User-Agent": USER_AGENT})


def _normalize_text(value: Optional[str]) -> str:
    if not value:
        return ""
    normalized = re.sub(r"[^a-z0-9]+", " ", str(value).casefold())
    return " ".join(normalized.split())


def _cache_get(key: str) -> Any:
    entry = _CACHE.get(key)
    if entry is None:
        return None
    if entry.is_expired:
        _CACHE.pop(key, None)
        return None
    return entry.value


def _cache_set(key: str, value: Any) -> None:
    _CACHE[key] = CacheEntry(
        value=value,
        expires_at=datetime.now(timezone.utc) + timedelta(seconds=CACHE_TTL_SECONDS),
    )


def _request_json(path: str, params: Optional[dict[str, Any]] = None) -> dict[str, Any]:
    if params is None:
        params = {}
    query = urllib.parse.urlencode(params, doseq=True)
    cache_key = f"{path}?{query}"
    cached = _cache_get(cache_key)
    if cached is not None:
        return cached

    url = f"{OPENLIBRARY_BASE_URL}{path}"
    last_exc: Optional[Exception] = None
    for attempt in range(MAX_RETRIES + 1):
        try:
            response = _SESSION.get(url, params=params, timeout=HTTP_TIMEOUT_SECONDS)
            response.raise_for_status()
            payload = response.json()
            _cache_set(cache_key, payload)
            return payload
        except requests.RequestException as exc:
            last_exc = exc
            if attempt >= MAX_RETRIES:
                break
    raise RuntimeError(f"OpenLibrary request failed for {path}: {last_exc}")


def _subject_to_genre(subject: str) -> Optional[str]:
    normalized = _normalize_text(subject)
    if not normalized:
        return None
    for keyword, genre in SUBJECT_KEYWORDS_TO_GENRE.items():
        if keyword in normalized:
            return genre
    return None


def _enrich_owned_book(book: Book) -> tuple[List[str], Optional[str]]:
    raw_authors = [a for a in (book.authors or []) if _normalize_text(a) and _normalize_text(a) != "n a"]
    raw_genre = str(book.genre).strip().upper() if book.genre else ""
    if raw_authors and raw_genre and raw_genre != "UNKNOWN":
        return raw_authors, raw_genre

    if not book.title or not book.title.strip():
        return raw_authors, raw_genre if raw_genre else None

    try:
        payload = _request_json(
            "/search.json",
            {
                "title": book.title.strip(),
                "limit": 1,
                "language": "eng",
                "fields": "title,author_name,subject",
            },
        )
    except RuntimeError:
        return raw_authors, raw_genre if raw_genre else None
    docs = payload.get("docs")
    if not isinstance(docs, list) or not docs:
        return raw_authors, raw_genre if raw_genre else None

    doc = docs[0]
    authors = list(raw_authors)
    if not authors and isinstance(doc.get("author_name"), list):
        authors = [name for name in doc["author_name"] if isinstance(name, str) and name.strip()]

    genre = raw_genre if raw_genre and raw_genre != "UNKNOWN" else None
    if genre is None and isinstance(doc.get("subject"), list):
        for subject in doc["subject"]:
            if not isinstance(subject, str):
                continue
            mapped = _subject_to_genre(subject)
            if mapped:
                genre = mapped
                break

    return authors, genre


def _extract_authors_from_work(work: dict[str, Any]) -> List[str]:
    names: List[str] = []
    if isinstance(work.get("authors"), list):
        for author in work["authors"]:
            name = author.get("name") if isinstance(author, dict) else None
            if name:
                names.append(name)
    if not names and isinstance(work.get("author_name"), list):
        for name in work["author_name"]:
            if isinstance(name, str):
                names.append(name)
    return names


def _extract_primary_author_key(work: dict[str, Any]) -> Optional[str]:
    authors = work.get("authors")
    if isinstance(authors, list) and authors:
        first = authors[0]
        if isinstance(first, dict):
            key = first.get("key")
            if isinstance(key, str) and key.startswith("/authors/"):
                return key.split("/")[-1]
    return None


def _candidate_from_work(work: dict[str, Any]) -> Optional[Candidate]:
    title = work.get("title")
    if not isinstance(title, str) or not title.strip():
        return None
    work_key = work.get("key")
    authors = _extract_authors_from_work(work)
    primary_author = authors[0] if authors else ""
    if not isinstance(work_key, str) or not work_key.strip():
        work_key = f"title-author:{_normalize_text(title)}|{_normalize_text(primary_author)}"

    year = work.get("first_publish_year")
    publication_year = int(year) if isinstance(year, int) else None
    edition_count = work.get("edition_count")
    edition_count_int = int(edition_count) if isinstance(edition_count, int) else 0

    return Candidate(
        key=work_key,
        title=title.strip(),
        authors=authors,
        publication_year=publication_year,
        edition_count=max(0, edition_count_int),
    )


def _candidate_identity(title: str, authors: List[str]) -> str:
    primary_author = authors[0] if authors else ""
    return f"{_normalize_text(title)}|{_normalize_text(primary_author)}"


def _make_user_profile(owned_books: List[Book]) -> UserProfile:
    genre_counter: Counter[str] = Counter()
    author_counter: Counter[str] = Counter()
    owned_keys: set[str] = set()
    owned_author_set: set[str] = set()

    for book in owned_books:
        authors, genre = _enrich_owned_book(book)
        if genre:
            genre_norm = str(genre).strip().upper()
            if genre_norm and genre_norm != "UNKNOWN":
                genre_counter[genre_norm] += 1

        for author in authors:
            author_norm = _normalize_text(author)
            if author_norm:
                author_counter[author_norm] += 1
                owned_author_set.add(author_norm)

        if book.title:
            owned_keys.add(_candidate_identity(book.title, authors))

    return UserProfile(
        genre_counter=genre_counter,
        author_counter=author_counter,
        owned_keys=owned_keys,
        owned_author_set=owned_author_set,
    )


def _search_author_olid(author_name_normalized: str) -> Optional[str]:
    payload = _request_json("/search/authors.json", {"q": author_name_normalized, "limit": 5})
    docs = payload.get("docs")
    if not isinstance(docs, list):
        return None
    if not docs:
        return None
    key = docs[0].get("key")
    if isinstance(key, str) and key.startswith("/authors/"):
        return key.split("/")[-1]
    return None


def _fetch_subject_candidates(subject_slug: str) -> List[Candidate]:
    payload = _request_json(f"/subjects/{subject_slug}.json", {"details": "false", "limit": SUBJECT_LIMIT})
    works = payload.get("works")
    if not isinstance(works, list):
        return []
    out: List[Candidate] = []
    for work in works:
        if not isinstance(work, dict):
            continue
        candidate = _candidate_from_work(work)
        if candidate is None:
            continue
        out.append(candidate)
    return out


def _fetch_author_candidates(olid: str) -> List[Candidate]:
    payload = _request_json(f"/authors/{olid}/works.json", {"limit": AUTHOR_WORKS_LIMIT})
    entries = payload.get("entries")
    if not isinstance(entries, list):
        return []
    out: List[Candidate] = []
    for work in entries:
        if not isinstance(work, dict):
            continue
        candidate = _candidate_from_work(work)
        if candidate is None:
            continue
        out.append(candidate)
    return out


def _collect_candidates(profile: UserProfile) -> Dict[str, Candidate]:
    candidates: Dict[str, Candidate] = {}

    for genre, _freq in profile.genre_counter.most_common(MAX_GENRES):
        subject_slug = GENRE_TO_SUBJECT.get(genre)
        if not subject_slug:
            continue
        for candidate in _fetch_subject_candidates(subject_slug):
            existing = candidates.get(candidate.key)
            if existing is None:
                candidate.source_genres.add(genre)
                candidates[candidate.key] = candidate
            else:
                existing.source_genres.add(genre)

    for author_norm, _freq in profile.author_counter.most_common(MAX_AUTHORS):
        olid = _search_author_olid(author_norm)
        if not olid:
            continue
        for candidate in _fetch_author_candidates(olid):
            existing = candidates.get(candidate.key)
            if existing is None:
                candidate.source_authors.add(author_norm)
                candidates[candidate.key] = candidate
            else:
                existing.source_authors.add(author_norm)

    # Fallback: if user profile is too sparse or enrichment misses, keep UX alive.
    if not candidates:
        for candidate in _fetch_subject_candidates("fiction"):
            if candidate.key not in candidates:
                candidate.source_genres.add("FICTION")
                candidates[candidate.key] = candidate

    return candidates


def _compute_score(candidate: Candidate, profile: UserProfile) -> float:
    genre_total = sum(profile.genre_counter.values()) or 1
    genre_score = (
        sum(profile.genre_counter.get(genre, 0) for genre in candidate.source_genres) / genre_total
        if candidate.source_genres
        else 0.0
    )

    cand_authors = {_normalize_text(author) for author in candidate.authors if author}
    author_score = 1.0 if cand_authors.intersection(profile.owned_author_set) else 0.0

    popularity_score = min(1.0, (candidate.edition_count ** 0.5) / 20.0)
    current_year = datetime.now(timezone.utc).year
    recency_score = 0.0
    if candidate.publication_year:
        age = max(0, current_year - candidate.publication_year)
        recency_score = max(0.0, 1.0 - (age / 100.0))

    return (
        0.60 * genre_score
        + 0.30 * author_score
        + 0.07 * popularity_score
        + 0.03 * recency_score
    )


def make_recommendations(owned_books: List[Book] | Book, k: int, _unused_df: Any = None) -> List[Recommandation]:
    if isinstance(owned_books, Book):
        owned_books = [owned_books]
    if not owned_books:
        return []

    profile = _make_user_profile(owned_books)
    candidates = _collect_candidates(profile)

    scored: List[tuple[Candidate, float]] = []
    for candidate in candidates.values():
        identity = _candidate_identity(candidate.title, candidate.authors)
        if identity in profile.owned_keys:
            continue
        score = _compute_score(candidate, profile)
        scored.append((candidate, score))

    scored.sort(key=lambda item: item[1], reverse=True)

    out: List[Recommandation] = []
    for candidate, score in scored[: max(1, k)]:
        out.append(
            Recommandation(
                book=Book(
                    id=None,
                    isbn=None,
                    title=candidate.title,
                    authors=candidate.authors or None,
                    publisher=None,
                    publicationYear=candidate.publication_year,
                    genre=None,
                ),
                score=round(float(score), 6),
            )
        )
    return out
