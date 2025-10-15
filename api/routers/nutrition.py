from logging import Logger
from fastapi import APIRouter, HTTPException
import requests

from schemas.natural_request import NaturalRequest
from schemas.natural_response import NaturalResponse

from constants import NX_BASE, NX_HEADERS

# deal with logging later if ever
logger = Logger() 

router = APIRouter(
    prefix="/nutrition",
    tags=["nutrition"]
)

# necesary?
def _safe_float(value, default=0.0):
    """Safely convert to float, handling None, empty, or non-numeric values."""
    if value is None or value == "":
        return default
    try:
        return float(value)
    except (ValueError, TypeError):
        return default

@router.post("/natural", response_model=NaturalResponse)
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
            headers=NX_HEADERS,
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

@router.get("/instant")
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
            headers=NX_HEADERS,
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