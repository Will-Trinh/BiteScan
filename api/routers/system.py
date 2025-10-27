from fastapi import APIRouter

router = APIRouter(
    prefix="/sys",
    tags=["system"]
)

@router.post("/health")
def health():
    """Health check endpoint."""
    return {"ok": True}