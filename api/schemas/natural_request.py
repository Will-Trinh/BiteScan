from pydantic import BaseModel

class NaturalRequest(BaseModel):
    query: str
