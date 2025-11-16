from pydantic import BaseModel
from datetime import datetime

class Item(BaseModel):
    id: int | None = 0
    name: str | None = None
    price: float | None = None
    quantity: float | None = None
    date: datetime | None = None
    store: str | None = None
    category: str | None = None
    receiptId: int | None = None
    protein: float | None = None
    carbs: float | None = None
    fats: float | None = None
    calories: float | None = None
