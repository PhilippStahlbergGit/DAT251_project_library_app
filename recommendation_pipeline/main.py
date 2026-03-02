
from fastapi import FastAPI, HTTPException

from pydantic import BaseModel
from typing import List
import threading

try:
    from .models import Book
    from .Recommandation import make_recommendations, getData
except ImportError:
    from models import Book
    from Recommandation import make_recommendations, getData

app = FastAPI()

_data_lock = threading.Lock()
_data_df = None
_data_error: str | None = None
_data_loading = False


def _load_data_worker() -> None:
    global _data_df, _data_error, _data_loading

    with _data_lock:
        if _data_loading:
            return
        _data_loading = True
        _data_error = None

    try:
        df = getData()
        with _data_lock:
            _data_df = df
    except Exception as exc:
        with _data_lock:
            _data_error = str(exc)
    finally:
        with _data_lock:
            _data_loading = False


def _start_background_data_load(force: bool = False) -> None:
    with _data_lock:
        should_start = force or (_data_df is None and not _data_loading)

    if should_start:
        threading.Thread(target=_load_data_worker, daemon=True).start()


@app.on_event("startup")
def warmup_data() -> None:
    _start_background_data_load()

class RecommendRequest(BaseModel):
    owned_books: List[Book] | Book
    k: int = 10

@app.post("/recommend")
def recommend(req: RecommendRequest):

    # simple error handling
    if req.owned_books is None or len(req.owned_books) == 0:
        raise HTTPException(status_code=400, detail="owned_books must be provided and cannot be empty.")
    if req.k <= 0:
        raise HTTPException(status_code=400, detail="k must be a positive integer.")        

    try:
        return {"recommendations": make_recommendations(req.owned_books, req.k)}
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@app.get("/data")
def getDataEndpoint(limit: int = 200, offset: int = 0):
    # For testing/inspection: returns a paginated view of data when preloading is ready.
    if limit <= 0 or offset < 0:
        raise HTTPException(status_code=400, detail="limit must be > 0 and offset must be >= 0.")

    with _data_lock:
        df = _data_df
        error = _data_error
        loading = _data_loading

    if df is None:
        if error:
            raise HTTPException(status_code=500, detail=f"Background data load failed: {error}")

        if not loading:
            _start_background_data_load()

        raise HTTPException(
            status_code=202,
            detail="Data is loading in the background. Check /data/status and retry shortly.",
        )

    total_rows = len(df.index)
    chunk = df.iloc[offset : offset + limit]
    return {
        "meta": {"total_rows": total_rows, "offset": offset, "limit": limit, "returned_rows": len(chunk.index)},
        "data": chunk.to_dict(orient="records"),
    }


@app.get("/data/status")
def getDataStatus():
    with _data_lock:
        df = _data_df
        error = _data_error
        loading = _data_loading

    return {
        "loading": loading,
        "ready": df is not None,
        "rows": 0 if df is None else len(df.index),
        "error": error,
    }


@app.post("/data/reload")
def reloadData():
    global _data_df
    with _data_lock:
        _data_df = None
    _start_background_data_load(force=True)
    return {"message": "Background reload started."}