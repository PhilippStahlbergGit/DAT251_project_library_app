
from fastapi import FastAPI

from pydantic import BaseModel
from typing import List, Optional, Dict

from Recommandation import make_recommendations

app = FastAPI()

class RecommendRequest(BaseModel):
    owned_book_ids: List[int]
    k: int = 10
    filters: Optional[Dict] = None


@app.post("/recommend")
def recommend(req: RecommendRequest):
    return {"recommendations": make_recommendations(req.owned_book_ids, req.k, req.filters)}
