from pymysql.connections import Connection

import settings
from services.auth import generate_session_token

# noinspection PyTypeChecker
now_connection: Connection = None


def try_connect() -> Connection:
    return Connection(
        host=settings.DB_HOST,
        user=settings.DB_USER_NAME,
        password=settings.DB_PASSWORD,
        db=settings.DB_NAME,
    )


def get_connection() -> Connection:
    global now_connection
    if now_connection is not None and now_connection.ping(reconnect=True):
        return now_connection
    now_connection = try_connect()
    return now_connection


def close_now_connection() -> None:
    global now_connection
    if now_connection is not None:
        now_connection.close()
    now_connection = None


def test_connect_db():
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT 1")


def registration(name: str, email: str, password: str):
    with get_connection().cursor() as cursor:
        cursor.execute("INSERT INTO users (name, email, password) VALUES (%s, %s, %s)", (name, email, password))
        user_id = cursor.lastrowid
        cursor.connection.commit()

    return user_id


def check_user_email_and_name_for_existence(email: str, name: str) -> bool:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT 1 FROM users WHERE email = %s OR name = %s", (email, name))
        return cursor.fetchone() is not None


def try_auth_and_create_session(login: str, password: str) -> None | tuple[str, int, str, int]:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT id FROM users WHERE (email = %s or name = %s) AND password = %s", (login, login, password))
        data = cursor.fetchone()
        if data is None:
            return None
        else:
            token = generate_session_token()
            application_token = generate_session_token()
            cursor.execute("INSERT INTO sessions (user_id, data, last_time, token) VALUES (%s, %s, NOW(), %s)", (data[0], '{}', token))
            session_id = cursor.lastrowid
            cursor.execute("INSERT INTO application_sessions (user_id, last_time, token) VALUES (%s, NOW(), %s)", (data[0], application_token))
            application_session_id = cursor.lastrowid
            cursor.connection.commit()
            return token, session_id, application_token, application_session_id


def try_auth_application_and_create_session(token: str, application_session_id: int) -> None | tuple[str, int]:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT id, user_id FROM application_sessions WHERE token=%s AND id = %s", (token, application_session_id))
        data = cursor.fetchone()
        if data is None:
            return None
        else:
            token = generate_session_token()
            cursor.execute("INSERT INTO sessions (user_id, data, last_time, token) VALUES (%s, %s, NOW(), %s)", (data[1], '{}', token))
            session_id = cursor.lastrowid
            cursor.connection.commit()
            return token, session_id


def clear_sessions() -> None:
    with get_connection().cursor() as cursor:
        cursor.execute("DELETE FROM sessions WHERE last_time < NOW() - INTERVAL 10 MINUTE")
        cursor.connection.commit()
