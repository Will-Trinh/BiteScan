from typing import Annotated
from fastapi import APIRouter, status, UploadFile, Depends, HTTPException

from dependencies import oauth2_scheme

from subprocess import run, CompletedProcess, CalledProcessError

import base64
import json
import os
import tempfile 


router = APIRouter(
    prefix="/receipts",
    #prefix="/receipts",
    tags=["receipts"]
)
