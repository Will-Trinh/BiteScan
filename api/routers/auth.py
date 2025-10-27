from typing import Annotated
from fastapi import APIRouter, Depends

router = APIRouter(
    prefix="/auth",
    tags=["authentication"]
)

@router.post("/token")
async def create_token():
    return {"token": "123"}