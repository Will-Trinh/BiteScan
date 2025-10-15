"""
nutritionapi.py - Nutritionix API Proxy for BiteScan

Securely proxies nutrition lookups between Android app and Nutritionix API.
Keeps API credentials server-side and provides clean, normalized responses.

Endpoints:
- POST /api/nutritionix/natural: Free-text food query → nutrition data
- GET  /api/nutritionix/instant: Autocomplete/search suggestions
"""

# --- standard library ---
import logging
import os
from typing import Optional

# --- third-party ---
import requests
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger(__name__)

# config
APP_ID = os.getenv("NUTRITIONIX_APP_ID")
APP_KEY = os.getenv("NUTRITIONIX_APP_KEY")
if not APP_ID or not APP_KEY:
    raise RuntimeError("Set NUTRITIONIX_APP_ID and NUTRITIONIX_APP_KEY env vars")

NX_BASE = "https://trackapi.nutritionix.com/v2"
HEADERS = {
    "x-app-id": APP_ID,
    "x-app-key": APP_KEY,
    "Content-Type": "application/json"
}

# --- helpers ---
def _safe_float(value, default=0.0):
    """Safely convert to float, handling None, empty, or non-numeric values."""
    if value is None or value == "":
        return default
    try:
        return float(value)
    except (ValueError, TypeError):
        return default

# --- models ---
class NaturalRequest(BaseModel):
    query: str

class Macros(BaseModel):
    calories: float
    protein: float
    carbs: float
    fat: float

class NaturalResponse(BaseModel):
    status: str
    name: Optional[str] = None
    serving_qty: Optional[float] = None
    serving_unit: Optional[str] = None
    serving_weight_grams: Optional[float] = None
    macros: Optional[Macros] = None

app = FastAPI(title="BiteScan Nutrition API", version="1.0.0")

@app.get("/")
def health():
    """Health check endpoint."""
    return {"ok": True, "service": "nutritionapi"}

@app.post("/api/nutritionix/natural", response_model=NaturalResponse)
def natural(req: NaturalRequest):
    """
    Proxy for Nutritionix natural language food query.

    Takes free-text (e.g., "2 eggs and bacon") and returns normalized nutrition data.
    Returns first matched food item or status="missing" if none found.
    """
    if not req.query or not req.query.strip():
        raise HTTPException(status_code=400, detail="Query cannot be empty")

    logger.info(f"Natural query: {req.query}")

    try:
        r = requests.post(
            f"{NX_BASE}/natural/nutrients",
            headers=HEADERS,
            json={"query": req.query},
            timeout=12,
        )
    except requests.RequestException as e:
        logger.error(f"Network error calling Nutritionix: {e}")
        raise HTTPException(status_code=502, detail=f"Upstream error: {e.__class__.__name__}")

    if r.status_code >= 400:
        logger.warning(f"Nutritionix returned {r.status_code} for query: {req.query}")
        raise HTTPException(status_code=r.status_code, detail="Nutrition provider error")

    try:
        data = r.json()
    except requests.JSONDecodeError:
        logger.error("Invalid JSON from Nutritionix")
        raise HTTPException(status_code=502, detail="Invalid JSON from nutrition provider")

    foods = data.get("foods") or []
    if not foods:
        logger.info(f"No foods found for query: {req.query}")
        return NaturalResponse(status="missing")

    f = foods[0]
    macros = Macros(
        calories=_safe_float(f.get("nf_calories")),
        protein=_safe_float(f.get("nf_protein")),
        carbs=_safe_float(f.get("nf_total_carbohydrate")),
        fat=_safe_float(f.get("nf_total_fat")),
    )

    logger.info(f"Matched: {f.get('food_name')} - {macros.calories} cal")

    return NaturalResponse(
        status="ok",
        name=f.get("food_name"),
        serving_qty=f.get("serving_qty"),
        serving_unit=f.get("serving_unit"),
        serving_weight_grams=f.get("serving_weight_grams"),
        macros=macros,
    )

@app.get("/api/nutritionix/instant")
def instant(query: str):
    """
    Proxy for Nutritionix instant search (autocomplete).

    Used by manual-fix UI to provide food suggestions as user types.
    Returns common and branded food matches.
    """
    if not query or not query.strip():
        raise HTTPException(status_code=400, detail="Query parameter cannot be empty")

    logger.info(f"Instant search: {query}")

    try:
        r = requests.get(
            f"{NX_BASE}/search/instant",
            headers=HEADERS,
            params={"query": query},
            timeout=8,
        )
    except requests.RequestException as e:
        logger.error(f"Network error calling Nutritionix instant: {e}")
        raise HTTPException(status_code=502, detail=f"Upstream error: {e.__class__.__name__}")

    if r.status_code >= 400:
        logger.warning(f"Nutritionix instant returned {r.status_code} for query: {query}")
        raise HTTPException(status_code=r.status_code, detail="Nutrition provider error")

    try:
        result = r.json()
        logger.info(f"Instant results: {len(result.get('common', []))} common, {len(result.get('branded', []))} branded")
        return result
    except requests.JSONDecodeError:
        logger.error("Invalid JSON from Nutritionix instant")
        raise HTTPException(status_code=502, detail="Invalid JSON from nutrition provider")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("nutritionapi:app", host="0.0.0.0", port=8080, reload=True)
