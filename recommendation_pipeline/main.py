from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List

try:
    from .models import Book
    from .Recommandation import make_recommendations
except ImportError:
    from models import Book
    from Recommandation import make_recommendations

app = FastAPI()


class RecommendRequest(BaseModel):
    owned_books: List[Book] | Book
    k: int = 10


@app.post("/recommend")
def recommend(req: RecommendRequest):
    owned_books = req.owned_books if isinstance(req.owned_books, list) else [req.owned_books]

    if not owned_books:
        raise HTTPException(status_code=400, detail="owned_books must be provided and cannot be empty.")
    if req.k <= 0:
        raise HTTPException(status_code=400, detail="k must be a positive integer.")

    try:
        return {"recommendations": make_recommendations(owned_books, req.k)}
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except RuntimeError as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc


@app.get("/data")
def get_data_endpoint(limit: int = 200, offset: int = 0):
    if limit <= 0 or offset < 0:
        raise HTTPException(status_code=400, detail="limit must be > 0 and offset must be >= 0.")
    return {
        "meta": {"total_rows": 0, "offset": offset, "limit": limit, "returned_rows": 0},
        "data": [],
        "message": "Deprecated endpoint: recommendation data is fetched live from OpenLibrary.",
    }


@app.get("/data/status")
def get_data_status():
    return {
        "loading": False,
        "ready": True,
        "rows": 0,
        "error": None,
        "message": "Deprecated endpoint: recommendation data is fetched live from OpenLibrary.",
    }


@app.post("/data/reload")
def reload_data():
    return {"message": "No-op. Recommendation data is fetched live from OpenLibrary."}

