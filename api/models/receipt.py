from sqlmodel import SQLModel, Field
from datetime import date

class Receipt(SQLModel, table=True):
    __tablename__ = "receipts"
    id: int = Field(default=None, primary_key=True)
    user_id: int = Field(foreign_key='users.id', nullable=False)
    store: str = Field()
    purchase_date: date = Field()
