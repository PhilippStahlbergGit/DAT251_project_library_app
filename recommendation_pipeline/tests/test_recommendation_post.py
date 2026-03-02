from fastapi.testclient import TestClient
from recommendation_pipeline.main import app


client = TestClient(app)


def test_recommend_valid_request():
    response = client.post(
        "/recommend",
        json={
            "owned_books": [
                {"title": "Book A", "author": "Author A"},
                {"title": "Book B", "author": "Author B"},
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