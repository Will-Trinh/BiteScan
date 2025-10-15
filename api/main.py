# sms (queue) -> run lambda function
# this current file will otherwise run 24/7 on ec2

from fastapi import FastAPI, WebSocket, WebSocketDisconnect, UploadFile, status, Depends
from fastapi.middleware.cors import CORSMiddleware
from routers import receipts, users, auth


app = FastAPI()

app.include_router(users.router)
app.include_router(receipts.router)
app.include_router(auth.router)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"]
)
