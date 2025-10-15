from pydantic import BaseModel

class Macros(BaseModel):
    calories: float
    protein: float
    carbs: float
    fat: float

