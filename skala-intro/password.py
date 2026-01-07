# password.py
import re

PASSWORD_PATTERN = re.compile(
    r'^(?=.{6,}$)(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).*$'
)

def validate_password(password: str) -> bool:
    return bool(PASSWORD_PATTERN.match(password))
