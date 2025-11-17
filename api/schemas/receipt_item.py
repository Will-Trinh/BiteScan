from pydantic import BaseModel

class ReceiptItem(BaseModel):
    sequence: int
    name: str
    quantity: int
    category: str
    protein: float | None
    fats: float | None
    calories: float | None