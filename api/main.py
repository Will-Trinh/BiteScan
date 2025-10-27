# sms (queue) -> run lambda function
# this current file will otherwise run 24/7 on ec2

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi import HTTPException
import base64
from io import BytesIO
from PIL import Image
import subprocess  # For calling your receipt-ocr CLI
import json
import os
import tempfile  # For temp file handling

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

@app.post("/ocr")
async def ocr_receipt(data: dict):
    print(f"Received POST to /ocr with image length: {len(data.get('image', ''))}")
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
        result = subprocess.run(["receipt-ocr", temp_path], capture_output=True, text=True, check=True)
        os.unlink(temp_path)  # Clean up temp file

        # Parse CLI output as JSON
        parsed_result = json.loads(result.stdout.strip())
        print(f"CLI output parsed: {parsed_result}")
        return parsed_result
    except subprocess.CalledProcessError as e:
        print(f"CLI error: {e.stderr}")
        raise HTTPException(status_code=500, detail=f"OCR CLI failed: {e.stderr}")
    except json.JSONDecodeError as e:
        print(f"JSON parse error: {e}")
        raise HTTPException(status_code=500, detail="Invalid JSON from OCR")
    except Exception as e:
        print(f"OCR endpoint error: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000, reload=True)
