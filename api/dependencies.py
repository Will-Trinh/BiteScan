from fastapi.security import OAuth2PasswordBearer
from fastapi import Depends
from typing import Annotated

from schemas.user import User

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

#region users

def decode_token(token: str) -> User:
    return User()

async def get_current_user(token: Annotated[str, Depends(oauth2_scheme)]) -> User:
    return decode_token(token)

#endregion