from typing import Annotated
from fastapi import APIRouter, status, UploadFile, Depends

from dependencies import oauth2_scheme

from subprocess import run, CompletedProcess
from json import dumps

router = APIRouter(
    prefix="/receipts",
    tags=["receipts"]
)

@router.post("", status_code=status.HTTP_200_OK)
async def scan_receipt(token: Annotated[str, Depends(oauth2_scheme)], file: UploadFile):
    scanned_receipt: CompletedProcess = run(["receipt-ocr", "./test_images/costco.jpg"], capture_output=True)
    # run(f"receipt-ocr {file}")
    return {"result": dumps(scanned_receipt.stdout)}