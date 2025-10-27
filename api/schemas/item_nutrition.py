# deprecated prob

from pydantic import BaseModel
from item import Item

class ItemNutrition(BaseModel, Item):
    name: str
    protein: float
    carbs: float
    fats: float
    calories: float
