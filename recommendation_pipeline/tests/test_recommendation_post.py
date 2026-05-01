import sys
from pathlib import Path

from fastapi.testclient import TestClient

# NOTE: hard coded, but works for now
_HERE = Path(__file__).resolve()
_PIPELINE_DIR = _HERE.parents[1]
_REPO_ROOT = _HERE.parents[2]
for _path in (str(_REPO_ROOT), str(_PIPELINE_DIR)):
    if _path not in sys.path:
        sys.path.insert(0, _path)

try:
    import recommendation_pipeline.Recommandation as rec_module
    from recommendation_pipeline.main import app
except ModuleNotFoundError:
    import Recommandation as rec_module
    from main import app


client = TestClient(app)


def _mock_openlibrary_request(path: str, params=None):
    if path.startswith("/subjects/romance.json"):
        return {
            "works": [
                {
                    "key": "/works/OL1W",
                    "title": "Romance Pick",
                    "authors": [{"name": "Author Z", "key": "/authors/OLZ1A"}],
                    "first_publish_year": 2019,
                    "edition_count": 20,
                },
                {
                    "key": "/works/OL2W",
                    "title": "Owned Book",
                    "authors": [{"name": "Daniel Kahneman", "key": "/authors/OLK1A"}],
                    "first_publish_year": 2011,
                    "edition_count": 12,
                },
            ]
        }
    if path == "/search/authors.json":
        return {"docs": [{"key": "/authors/OLK1A"}]}
    if path == "/authors/OLK1A/works.json":
        return {
            "entries": [
                {
                    "key": "/works/OL3W",
                    "title": "Thinking Fast and Slow Companion",
                    "authors": [{"name": "Daniel Kahneman", "key": "/authors/OLK1A"}],
                    "first_publish_year": 2013,
                    "edition_count": 15,
                }
            ]
        }
    return {}


def test_recommend_valid_request(monkeypatch):
    monkeypatch.setattr(rec_module, "_request_json", _mock_openlibrary_request)

    response = client.post(
        "/recommend",
        json={
            "owned_books": [
                {"title": "Owned Book", "authors": ["Daniel Kahneman"], "genre": "ROMANCE"},
            ],
            "k": 5,
        },
    )
    assert response.status_code == 200
    payload = response.json()
    assert "recommendations" in payload
    assert len(payload["recommendations"]) >= 1
    titles = [rec["book"]["title"] for rec in payload["recommendations"]]
    assert "Owned Book" not in titles


def test_recommend_empty_owned_books():
    response = client.post(
        "/recommend",
        json={
            "owned_books": [],
            "k": 5,
        },
    )
    assert response.status_code == 400
    assert "empty" in response.json()["detail"]


def test_recommend_invalid_k():
    response = client.post(
        "/recommend",
        json={
            "owned_books": [
                {"title": "Book A", "author": "Author A"},
            ],
            "k": 0,
        },
    )
    assert response.status_code == 400
    assert "k must be a positive integer" in response.json()["detail"]
