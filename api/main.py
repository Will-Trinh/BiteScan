from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from routers import users, receipts, auth, system, nutrition

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
