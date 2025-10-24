from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager

from routers import receipts, users, auth, system, nutrition
from database import create_db_and_tables

app = FastAPI()

app.include_router(users.router)
app.include_router(receipts.router)
app.include_router(auth.router)
app.include_router(system.router)
app.include_router(nutrition.router)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"]
)

@asynccontextmanager
async def lifespan(app: FastAPI):
    # startup
    create_db_and_tables()
    yield
    # shutdown
