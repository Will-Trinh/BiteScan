from fastapi import APIRouter, HTTPException, status

from avocavo import IngredientResult
from avocavo_client import client

from schemas.item import Item

router = APIRouter(
    prefix="/nutrition",
    tags=["nutrition"]
)

@router.post("/items", status_code=200)
async def get_nutrition(items: list[Item]):
    if not items:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, detail="items missing")

    # modularize this later
    # gotta split into batches of 3 cuz avo free only allow 3 yay..
    # batches dont work.. more yay..
    """
    MAX_PER_BATCH = 3
    batches: list[list[str]] = []
    for i in items:
        if not batches or len(batches[-1]) >= MAX_PER_BATCH:
            batches.append([])
        batches[-1].append(i)

    for b in batches:
        print(b)
        result: BatchResult = client.analyze_ingredient(b)
        print(result)
        break
    """

    for i in items:
        if i.calories is not None:
            print(f"skipping {i.name}, data already present")
            continue
        try:
            result: IngredientResult = client.analyze_ingredient(i.name)
            i.calories = result.nutrition.calories
            i.carbs = result.nutrition.carbohydrates_total
            i.fats = result.nutrition.total_fat_total
            i.protein = result.nutrition.protein_total
            print(f"Nutirion hit: {i.name}")
        except:
            print(f"Validation Error: no nutrition found for item - {i.name}")

    print("analsyis complete")
    return items