from dotenv import load_dotenv
from os import getenv

load_dotenv()

APP_ID = getenv("NUTRITIONIX_APP_ID")
APP_KEY = getenv("NUTRITIONIX_APP_KEY")

NX_BASE = "https://trackapi.nutritionix.com/v2"
NX_HEADERS = {
    "x-app-id": APP_ID,
    "x-app-key": APP_KEY,
    "Content-Type": "application/json"
}