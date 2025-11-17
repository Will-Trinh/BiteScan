from sqlmodel import SQLModel, Field, Relationship

class ReceiptItem(SQLModel, table=True):
    __tablename__ = "receipt_items"
    receipt_id: int = Field(primary_key=True, foreign_key='receipts.id')
    user_id: int = Field(primary_key=True, foreign_key='users.id')
    sequence: int = Field(primary_key=True)
    name: str = Field()
    quantity: int = Field()
    category: str = Field(nullable=True)
    protein: float = Field(nullable=True)
    fats: float = Field(nullable=True)
    calories: float = Field(nullable=True)
