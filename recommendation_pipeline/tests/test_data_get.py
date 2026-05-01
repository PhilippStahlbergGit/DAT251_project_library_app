from fastapi.testclient import TestClient
from recommendation_pipeline.main import app


client = TestClient(app)


def test_get_data_returns_deprecated_shape():
    response = client.get("/data")
    assert response.status_code == 200
    payload = response.json()
    assert "data" in payload
    assert "meta" in payload
    assert payload["meta"]["returned_rows"] == 0


def test_get_data_status_always_ready():
    response = client.get("/data/status")
    assert response.status_code == 200
    payload = response.json()
    assert payload["ready"] is True
    assert payload["loading"] is False
    assert payload["rows"] == 0

