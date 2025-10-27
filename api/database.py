from sqlmodel import SQLModel, Session, create_engine
from constants import DATABASE_CONNECTION_STRING

connect_args = {"check_same_thread": False}
engine = create_engine(DATABASE_CONNECTION_STRING, connect_args)

def create_db_and_tables():
    SQLModel.metadata.create_all(engine)

def get_session():
    with Session(engine) as s:
        yield s