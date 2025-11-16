from typing import Annotated
from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session, select, update
from sqlalchemy.exc import IntegrityError

from database import get_session
from dependencies import get_current_user

from models.user import User
from models.receipt import Receipt
from models.receipt_item import ReceiptItem

from schemas.signup import Signup as SignupSchema
from schemas.login import Login as LoginSchema
from schemas.reset_password import ResetPassowrd as ResetPasswordSchema

router = APIRouter(
    prefix="/users",
    tags=["users"]
)

@router.get("/me")
async def read_users_me(current_user: Annotated[User, Depends(get_current_user)]):
    return current_user

#region profile

@router.post("/signup", status_code=201)
async def register(register: SignupSchema, session: Session = Depends(get_session)):
    try:
        db_user = User(**register.model_dump()) 
        session.add(db_user)
        session.commit()
        session.refresh(db_user)
        return db_user
    except IntegrityError:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="email already in use"
        )

@router.post("/login", status_code=200)
async def login(login: LoginSchema, session: Session = Depends(get_session)):
    stmt = select(User).where(
        (User.email == login.email) & (User.password == login.password) & (User.disabled == False)
    )

    result = session.exec(stmt)
    user = result.first()

    if user:
        return user
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Invalid email or password"
    )

# use same subset of properties from signup to perform update
@router.patch("{id}", status_code=204)
def update_user(id: int, user: SignupSchema, session: Session = Depends(get_session)):
    stmt = (
        update(User)
        .where(User.id == id) 
        .values(
            email=user.email,
            username=user.username,
            password=user.password
        )
    )

    session.exec(stmt)
    session.commit()
    return 

@router.patch("password-reset/{id}", status_code=204)
def update_password(id: int, reset: ResetPasswordSchema, session: Session = Depends(get_session)):
    stmt = (
        update(User)
        .where(User.id == id) 
        .values(password=reset.new_password)
    )

    session.exec(stmt)
    session.commit()
    return 

@router.delete("{id}", status_code=204)
def delete_user(id: int, session: Session = Depends(get_session)):
    stmt = (
        update(User)
        .where(User.id == id) 
        .values(disabled=True)
    )

    session.exec(stmt)
    session.commit()
    return 

#endregion

#region receipts

@router.get("/{user_id}/receipts", status_code=200)
def get_user_receipts(user_id: int, session: Session = Depends(get_session)):
    stmt = (
        select(Receipt, ReceiptItem)
        .join(ReceiptItem, ReceiptItem.receipt_id == Receipt.id)
        .where(Receipt.user_id == user_id)
    )

    return session.exec(stmt).fetchall()

@router.post("/{user_id}/receipts", status_code=201)
def create_receipt(user_id: int, session: Session = Depends(get_session)):
    pass

#endregion