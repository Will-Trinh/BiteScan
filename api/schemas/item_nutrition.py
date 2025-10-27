from pydantic import BaseModel

class ItemNutrition(BaseModel):
    name: str
    protein: float
    carbs: float
    fats: float
    calories: float
