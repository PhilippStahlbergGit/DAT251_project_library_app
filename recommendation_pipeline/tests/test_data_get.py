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
    from recommendation_pipeline.main import app
except ModuleNotFoundError:
    from main import app


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
