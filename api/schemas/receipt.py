from pydantic import BaseModel
from schemas.receipt_item import ReceiptItem
from datetime import date

class Receipt(BaseModel):
    id: int
    user_id: int
    store: str
    purchase_date: date
    items: list[ReceiptItem]