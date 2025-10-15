# sms (queue) -> run lambda function
# this current file will otherwise run 24/7 on ec2

from typing import Annotated
from fastapi import FastAPI, WebSocket, WebSocketDisconnect, UploadFile, status, Depends
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import OAuth2PasswordBearer

from subprocess import run, CompletedProcess
from json import dumps

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"]
)

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

@app.post("/receipt", status_code=status.HTTP_200_OK)
async def scan_receipt(token: Annotated[str, Depends(oauth2_scheme)], file: UploadFile):
    scanned_receipt: CompletedProcess = run(["receipt-ocr", "./test_images/costco.jpg"], capture_output=True)
    # run(f"receipt-ocr {file}")
    return {"result": dumps(scanned_receipt.stdout)}

