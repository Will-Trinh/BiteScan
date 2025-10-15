from typing import Annotated
from fastapi import APIRouter, Depends

from dependencies import get_current_user
from schemas.user import User

router = APIRouter(
    prefix="/users",
    tags=["users"]
)

@router.get("/me")
async def read_users_me(current_user: Annotated[User, Depends(get_current_user)]):
    return current_user