from pydantic import BaseModel
from datetime import date

class ReceiptPost(BaseModel):
    id: int
    store: str
    purchase_date: date
    status: str