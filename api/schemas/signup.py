from pydantic import BaseModel, EmailStr

class Signup(BaseModel):
    email: EmailStr
    username: str
    password: str