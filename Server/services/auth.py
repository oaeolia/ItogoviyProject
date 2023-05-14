from random import randint

import settings
from services import db

import hashlib
import re


def auth(login: str, password: str) -> str | tuple[str, int, str, int, int]:
    name_re = re.compile(r'^[a-zA-Zа-яА-Я0-9_-]{3,20}$')
    email_re = re.compile(r'^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$')
    if re.match(name_re, login) is None and re.match(email_re, login):
        return 'Login is not valid'

    password = hashlib.sha256(password.encode()).hexdigest()

    result = db.try_auth_and_create_session(login, password)
    if result is None:
        return 'Wrong login or password'
    else:
        return result


def auth_application(application_session_token: str, application_session_id: int) -> str | tuple[str, int, int]:
    token_re = re.compile(r'^[a-zA-Z0-9]{32}$')
    if re.match(token_re, application_session_token) is None or type(application_session_id) is not int:
        return 'Token or id is not valid'

    result = db.try_auth_application_and_create_session(application_session_token, application_session_id)
    if result is None:
        return 'Wrong auth data'
    else:
        return result


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


def logout_from_application(session_id: int, session_token: str) -> None:
    db.remove_application_session(session_id, session_token)
    db.close_now_connection()


def generate_session_token() -> str:
    result = ''
    for i in range(settings.TOKEN_LENGTH):
        result += str(settings.TOKEN_SYMBOLS[randint(0, len(settings.TOKEN_SYMBOLS) - 1)])
    return result


def get_session(session_id: int, session_token: str) -> dict | None:
    buffer = db.get_session(session_id, session_token)
    db.close_now_connection()
    return buffer


def clear_sessions() -> None:
    db.clear_sessions()
    db.close_now_connection()
