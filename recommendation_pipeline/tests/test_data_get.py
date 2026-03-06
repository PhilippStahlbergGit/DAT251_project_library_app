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


def test_get_data_when_ready_returns_200_and_data():
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
            }
        ]
    )
    _set_cache(df=sample, loading=False, error=None)

    response = client.get("/data")
    assert response.status_code == 200
    payload = response.json()
    assert "data" in payload
    assert "meta" in payload
    assert payload["meta"]["returned_rows"] == 1


def test_get_data_while_loading_returns_202():
    _set_cache(df=None, loading=True, error=None)

    response = client.get("/data")
    assert response.status_code == 202
    assert "loading" in response.json()["detail"].lower()


def test_get_data_status_reflects_cache_state():
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
            }
        ]
    )
    _set_cache(df=sample, loading=False, error=None)

    response = client.get("/data/status")
    assert response.status_code == 200
    payload = response.json()
    assert payload["ready"] is True
    assert payload["loading"] is False
    assert payload["rows"] == 1