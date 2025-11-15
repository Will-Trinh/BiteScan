from sqlmodel import SQLModel, Field

class ReceiptItem(SQLModel, table=True):
    __tablename__ = "receipt_items"
    receipt_id: int = Field(primary_key=True)
    user_id: int = Field(primary_key=True)
    sequence: int = Field(primary_key=True)
    name: str = Field()
    quantity: int = Field()
    category: str = Field()
    protein: float = Field()
    fats: float = Field()
    calories: float = Field()