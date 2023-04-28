from pymysql.connections import Connection

import settings

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
