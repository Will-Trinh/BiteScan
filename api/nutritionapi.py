# nutritionapi.py
import os
from typing import Optional
import requests
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

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

app = FastAPI()

@app.get("/")
def health():
    return {"ok": True}

@app.post("/api/nutritionix/natural", response_model=NaturalResponse)
def natural(req: NaturalRequest):
    """Minimal proxy: free-text -> normalized macros. One attempt, simple timeout."""
    try:
        r = requests.post(
            f"{NX_BASE}/natural/nutrients",
            headers=HEADERS,
            json={"query": req.query},
            timeout=12,
        )
    except requests.RequestException as e:
        # network error
        raise HTTPException(status_code=502, detail=f"Upstream error: {e.__class__.__name__}")

    if r.status_code >= 400:
        # pass through status; hide body to avoid leaking details
        raise HTTPException(status_code=r.status_code, detail="Nutrition provider error")

    try:
        data = r.json()
    except requests.JSONDecodeError:
        raise HTTPException(status_code=502, detail="Invalid JSON from nutrition provider")

    foods = data.get("foods") or []
    if not foods:
        return NaturalResponse(status="missing")

    f = foods[0]
    macros = Macros(
        calories=_safe_float(f.get("nf_calories")),
        protein=_safe_float(f.get("nf_protein")),
        carbs=_safe_float(f.get("nf_total_carbohydrate")),
        fat=_safe_float(f.get("nf_total_fat")),
    )
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
    """Minimal pass-through for suggestions (used by manual-fix UI)."""
    try:
        r = requests.get(
            f"{NX_BASE}/search/instant",
            headers=HEADERS,
            params={"query": query},
            timeout=8,
        )
    except requests.RequestException as e:
        raise HTTPException(status_code=502, detail=f"Upstream error: {e.__class__.__name__}")

    if r.status_code >= 400:
        raise HTTPException(status_code=r.status_code, detail="Nutrition provider error")

    try:
        return r.json()
    except requests.JSONDecodeError:
        raise HTTPException(status_code=502, detail="Invalid JSON from nutrition provider")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("nutritionapi:app", host="0.0.0.0", port=8080, reload=True)
