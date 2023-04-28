from services import db

import hashlib
import re

# TODO: Add auth function


def auth(login: str, password: str):
    pass


# TODO: Add register function
def registration(name: str, email: str, password: str) -> str:
    name_re = re.compile(r'^[a-zA-Zа-яА-Я0-9_-]{3,20}$')
    email_re = re.compile(r'^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$')
    if re.match(name_re, name) is None:
        return 'Name is not valid'
    if re.match(email_re, email) is None:
        return 'Email is not valid'

    if db.check_user_email_and_name_for_existence(email, name):
        return 'User with this email or name already exists'

    password = hashlib.sha256(password.encode()).hexdigest()
    db.registration(name, email, password)

    db.close_now_connection()

    return ''
