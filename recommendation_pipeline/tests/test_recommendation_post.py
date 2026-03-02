from fastapi.testclient import TestClient
import pandas as pd
from recommendation_pipeline.main import app
import recommendation_pipeline.main as main_module


client = TestClient(app)


def _set_cache(df=None, loading=False, error=None):
    with main_module._data_lock:
        main_module._data_df = df
        main_module._data_loading = loading
        main_module._data_error = error


def test_recommend_valid_request():
    sample = pd.DataFrame(
        [
            {
                "Id": 1,
                "Name": "Book A",
                "Authors": "Author A",
                "pagesNumber": 100,
                "PublishYear": 2020,
                "Rating": 4.2,
                "RatingDistTotal": 12,
            },
            {
                "Id": 2,
                "Name": "Book B",
                "Authors": "Author B",
                "pagesNumber": 220,
                "PublishYear": 2019,
                "Rating": 4.0,
                "RatingDistTotal": 21,
            },
            {
                "Id": 3,
                "Name": "Book C",
                "Authors": "Author C",
                "pagesNumber": 300,
                "PublishYear": 2018,
                "Rating": 4.5,
                "RatingDistTotal": 40,
            },
        ]
    )
    _set_cache(df=sample, loading=False, error=None)

    response = client.post(
        "/recommend",
        json={
            "owned_books": [
                {"title": "Book A", "authors": ["Author A"]},
                {"title": "Book B", "authors": ["Author B"]},
            ],
            "k": 5,
        },
    )
    assert response.status_code == 200
    assert "recommendations" in response.json()

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