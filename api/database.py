from sqlmodel import SQLModel, Session, create_engine
from constants import DATABASE_CONNECTION_STRING

engine = create_engine(DATABASE_CONNECTION_STRING)

def create_db_and_tables():
    SQLModel.metadata.create_all(engine)

def get_session():
    with Session(engine) as s:
        yield s