from typing import List, Optional

from pydantic import BaseModel


class Book(BaseModel):
    id: Optional[int] = None
    isbn: Optional[str] = None
    title: Optional[str] = None
    authors: Optional[List[str]] = None
    publisher: Optional[str] = None
    publicationYear: Optional[int] = None
    genre: Optional[str] = None
