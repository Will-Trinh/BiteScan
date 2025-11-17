from dotenv import load_dotenv
from os import getenv

load_dotenv()

#region NutritionX
"""
APP_ID = getenv("NUTRITIONIX_APP_ID")
APP_KEY = getenv("NUTRITIONIX_APP_KEY")

NX_BASE = "https://trackapi.nutritionix.com/v2"
NX_HEADERS = {
    "x-app-id": APP_ID,
    "x-app-key": APP_KEY,
    "Content-Type": "application/json"
}
"""
#endregion

#region db

DATABASE_CONNECTION_STRING = getenv("DATABASE_CONNECTION_STRING")

#endregion

#region avocavo

AVO_KEY = getenv("AVO_KEY")

#endregion

#region smtp

SMTP_KEY = getenv("SMTP_KEY")

#endregion
