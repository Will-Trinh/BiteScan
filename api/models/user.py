from sqlmodel import SQLModel, Field

class User(SQLModel, table=True):
    __tablename__ = "users"
    id: int = Field(default=None, primary_key=True)
    email: str = Field(unique=True, nullable=False)
    username: str = Field(nullable=False)
    password: str = Field(nullable=False)
    disabled: bool = Field(default=False, nullable=False)
    phone: str | None = Field(default=None, nullable=True)
    diet: str | None = Field(default=None, nullable=True)