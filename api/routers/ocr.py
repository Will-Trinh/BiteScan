from typing import Annotated
from fastapi import APIRouter, status, UploadFile, Depends, HTTPException

from dependencies import oauth2_scheme

from subprocess import run, CompletedProcess, CalledProcessError

import base64
import json
import os
import tempfile 


router = APIRouter(
    prefix="/ocr",
    #prefix="/receipts",
    tags=["ocr"]
)

# just use tran/hoangs code for now, reshape it later to match fastapi
@router.post("/")
async def ocr_receipt(data: dict):
    try:
        base64_image = data.get("image")
        if not base64_image:
            raise HTTPException(status_code=400, detail="No image provided")
        
        # Decode base64 to bytes and save to temp file (your CLI needs file path)
        image_bytes = base64.b64decode(base64_image)
        with tempfile.NamedTemporaryFile(suffix='.png', delete=False) as temp_file:
            temp_path = temp_file.name
            temp_file.write(image_bytes)
            temp_file.flush()

        # Call your receipt-ocr CLI (adjust path to script if needed, e.g., "./receipt-ocr")
        result: CompletedProcess = run(["receipt-ocr", temp_path], capture_output=True, text=True, check=True)
        os.unlink(temp_path)  # Clean up temp file

        # Parse CLI output as JSON
        parsed_result = json.loads(result.stdout.strip())

        for item in parsed_result["line_items"]:
            if not item.get("item_quantity"):
                item["item_quantity"] = 1
            if item.get("item_description"):
                item["item_name"] = item["item_description"]
            if item.get("item_total_price"):
                item["item_price"] = item["item_total_price"] / item["item_quantity"]
                item["item_total"] = item["item_total_pricee"]

        print(f"CLI output parsed: {parsed_result}")

        return parsed_result
    except CalledProcessError as e:
        print(f"CLI error: {e.stderr}")
        raise HTTPException(status_code=500, detail=f"OCR CLI failed: {e.stderr}")
    except json.JSONDecodeError as e:
        print(f"JSON parse error: {e}")
        raise HTTPException(status_code=500, detail="Invalid JSON from OCR")
    except Exception as e:
        print(f"OCR endpoint error: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
