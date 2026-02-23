
from typing import List, Optional, Dict
from pydantic import BaseModel

class Recommandation(BaseModel):
    book_name: str
    score: float


def convertIdToBookName(book_id: int) -> str:
    # TODO: implement this function later.
    return f"Book_{book_id}"

def make_recommendations(owned_book_ids: List[int], k: int, filters: Optional[Dict] = None) -> List[Recommandation]:
    # TOOD: implement the actual recommendation logic here.
    recommendations = []
    for i in range(k):
        book_name = convertIdToBookName(i + 1)
        recommendations.append(Recommandation(book_name=book_name, score=0.5))
    return recommendations
