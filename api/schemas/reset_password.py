from pydantic import BaseModel, EmailStr

class ResetPassowrd(BaseModel):
    new_password: str