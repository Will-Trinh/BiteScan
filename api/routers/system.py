from fastapi import APIRouter

router = APIRouter(
    prefix="/sys",
    tags=["system"]
)

@router.get("/health")
def health():
    """Health check endpoint."""
    return {"ok": True}