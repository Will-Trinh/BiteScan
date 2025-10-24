from pydantic import BaseModel
from typing import Optional
from .macros import Macros

class NaturalResponse(BaseModel):
    status: str
    name: Optional[str] = None
    serving_qty: Optional[float] = None
    serving_unit: Optional[str] = None
    serving_weight_grams: Optional[float] = None
    macros: Optional[Macros] = None

