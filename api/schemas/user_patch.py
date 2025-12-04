from pydantic import BaseModel

class UserPatch(BaseModel):
    email: str | None
    username: str | None
    first_name: str | None
    last_name: str | None
    password: str | None
    disabled: bool
    phone: str | None
    diet: str | None
