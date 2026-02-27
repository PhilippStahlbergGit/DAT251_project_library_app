
from fastapi import FastAPI, HTTPException

from pydantic import BaseModel
from typing import List

from models import Book

from Recommandation import make_recommendations, getData

app = FastAPI()

class RecommendRequest(BaseModel):
    owned_books: List[Book] | Book
    k: int = 10

@app.post("/recommend")
def recommend(req: RecommendRequest):
    try:
        return {"recommendations": make_recommendations(req.owned_books, req.k)}
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@app.get("/data")
def getDataEndpoint():
    # for testing purposes!
    try:
        df = getData()
        return {"data": df.to_dict(orient="records")}
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc