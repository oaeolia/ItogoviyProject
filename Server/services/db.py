import json
import random

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
        # noinspection PyBroadException
        try:
            now_connection.close()
        except Exception:
            pass
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


def try_auth_and_create_session(login: str, password: str) -> None | tuple[str, int, str, int, int]:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT id FROM users WHERE (email = %s or name = %s) AND password = %s",
                       (login, login, password))
        data = cursor.fetchone()
        if data is None:
            return None
        else:
            token = generate_session_token()
            application_token = generate_session_token()
            cursor.execute("INSERT INTO sessions (user_id, data, last_time, token) VALUES (%s, %s, NOW(), %s)",
                           (data[0], '{"user_id": ' + str(data[0]) + '}', token))
            session_id = cursor.lastrowid
            cursor.execute("INSERT INTO application_sessions (user_id, last_time, token) VALUES (%s, NOW(), %s)",
                           (data[0], application_token))
            application_session_id = cursor.lastrowid
            cursor.connection.commit()
            return token, session_id, application_token, application_session_id, data[0]


def try_auth_application_and_create_session(token: str, application_session_id: int) -> None | tuple[str, int, int]:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT id, user_id FROM application_sessions WHERE token=%s AND id = %s",
                       (token, application_session_id))
        data = cursor.fetchone()
        if data is None:
            return None
        else:
            token = generate_session_token()
            cursor.execute("INSERT INTO sessions (user_id, data, last_time, token) VALUES (%s, %s, NOW(), %s)",
                           (data[1], '{"user_id": ' + str(data[1]) + '}', token))
            session_id = cursor.lastrowid
            cursor.connection.commit()
            return token, session_id, data[1]


def remove_application_session(session_id: int, session_token: str) -> None:
    with get_connection().cursor() as cursor:
        cursor.execute("DELETE FROM application_sessions WHERE id = %s AND token = %s", (session_id, session_token))
        cursor.connection.commit()


def get_new_of_free_room(user_id: int) -> int:
    with get_connection().cursor() as cursor:
        cursor.execute(
            "SELECT id, user_1, user_2, user_3, user_4 FROM games_rooms WHERE start_checked_time IS NULL  AND (user_1 IS NULL OR user_2 IS NULL OR user_3 IS NULL OR user_4 IS NULL OR user_5 IS NULL)")
        data = cursor.fetchone()
        if data is None:
            cursor.execute("INSERT INTO games_rooms (now_word, user_1) VALUES ('', %s)", user_id)
            cursor.connection.commit()
            return cursor.lastrowid
        else:
            if data[1] is None:
                cursor.execute("UPDATE games_rooms SET user_1 = %s WHERE id = %s", (user_id, data[0]))
            elif data[2] is None:
                cursor.execute("UPDATE games_rooms SET user_2 = %s WHERE id = %s", (user_id, data[0]))
            elif data[3] is None:
                cursor.execute("UPDATE games_rooms SET user_3 = %s WHERE id = %s", (user_id, data[0]))
            elif data[4] is None:
                cursor.execute("UPDATE games_rooms SET user_4 = %s WHERE id = %s", (user_id, data[0]))
            else:
                cursor.execute("UPDATE games_rooms SET user_5 = %s WHERE id = %s", (user_id, data[0]))
            cursor.connection.commit()
            return data[0]


def get_session(session_id: int, session_token: str) -> dict | None:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT data FROM sessions WHERE id = %s AND token = %s", (session_id, session_token))
        data = cursor.fetchone()
        if data is None:
            return None
        else:
            return json.loads(data[0])


def check_game_room_for_user(room_id: int, user_id: int) -> str:
    with get_connection().cursor() as cursor:
        cursor.execute(
            "SELECT id, user_1, user_2, user_3, user_4, user_5, is_started, start_checked_time FROM games_rooms WHERE (user_1 = %s OR user_2 = %s OR user_3 = %s OR user_4 = %s OR user_5 = %s) AND id=%s",
            (user_id, user_id, user_id, user_id, user_id, room_id))
        data = cursor.fetchone()
        if data is None:
            return ''
        else:
            if data[6]:
                return 'STARTED'
            else:
                if data[1] is not None and data[2] is not None and data[3] is not None and data[4] is not None and data[5] is not None:
                    if data[7] is None:
                        return 'STARTING'
                    else:
                        return 'WAITING_CHECK'
                else:
                    return 'WAITING'


def start_checked_game(room_id: int) -> None:
    with get_connection().cursor() as cursor:
        cursor.execute("UPDATE games_rooms SET start_checked_time=NOW() WHERE id = %s", room_id)
        cursor.connection.commit()


def game_room_set_user_checked(room_id: int, user_id: int) -> bool:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT id, user_1, user_2, user_3, user_4, user_5, is_started  FROM games_rooms WHERE id=%s",
                       room_id)
        data = cursor.fetchone()
        if data is None:
            return False
        if data[1] == user_id:
            cursor.execute("UPDATE games_rooms SET checked_user_1 = 1 WHERE id = %s", room_id)
        elif data[2] == user_id:
            cursor.execute("UPDATE games_rooms SET checked_user_2 = 1 WHERE id = %s", room_id)
        elif data[3] == user_id:
            cursor.execute("UPDATE games_rooms SET checked_user_3 = 1 WHERE id = %s", room_id)
        elif data[4] == user_id:
            cursor.execute("UPDATE games_rooms SET checked_user_4 = 1 WHERE id = %s", room_id)
        elif data[5] == user_id:
            cursor.execute("UPDATE games_rooms SET checked_user_5 = 1 WHERE id = %s", room_id)
        else:
            return False
        cursor.connection.commit()
        return True


def check_game_room(room_id: int) -> bool:
    with get_connection().cursor() as cursor:
        cursor.execute(
            "SELECT checked_user_1, checked_user_2, checked_user_3, checked_user_4, checked_user_5 FROM games_rooms WHERE id = %s AND start_checked_time > NOW() - INTERVAL 10 MINUTE",
            room_id)
        data = cursor.fetchone()
        if data is None:
            cursor.execute("DELETE FROM games_rooms WHERE id = %s", room_id)
            cursor.connection.commit()
            return False
        elif data[0] == 1 and data[1] == 1 and data[2] == 1 and data[3] == 1 and data[4] == 1:
            return True
        else:
            return False


def auto_set_room_word(room_id: int) -> None:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT MAX(id) FROM words")
        word_count = cursor.fetchone()[0]
        cursor.execute('SELECT word FROM words WHERE id = %s', random.randint(1, word_count))
        word = cursor.fetchone()[0]
        cursor.execute("UPDATE games_rooms SET now_word = %s WHERE id = %s", (word, room_id))
        cursor.connection.commit()


def set_drawer(room_id: int) -> None:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT now_painter, user_1, user_2, user_3, user_4, user_5 FROM games_rooms WHERE id = %s",
                       room_id)
        data = cursor.fetchone()

        if data[0] is None:
            cursor.execute("UPDATE games_rooms SET now_painter = %s WHERE id = %s", (data[1], room_id))
        elif data[0] == data[1]:
            cursor.execute("UPDATE games_rooms SET now_painter = %s WHERE id = %s", (data[2], room_id))
        elif data[0] == data[2]:
            cursor.execute("UPDATE games_rooms SET now_painter = %s WHERE id = %s", (data[3], room_id))
        elif data[0] == data[3]:
            cursor.execute("UPDATE games_rooms SET now_painter = %s WHERE id = %s", (data[4], room_id))
        elif data[0] == data[4]:
            cursor.execute("UPDATE games_rooms SET now_painter = %s WHERE id = %s", (data[5], room_id))

        cursor.execute("UPDATE games_rooms SET start_time = NOW() WHERE id = %s", room_id)

        cursor.connection.commit()


def get_messages_of_game(room_id: int) -> list[str]:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT text FROM messages WHERE room_id = %s", room_id)
        data = cursor.fetchall()
        if data is None:
            return []
        else:
            return list(map(lambda x: x[0], data))


def clear_sessions() -> None:
    with get_connection().cursor() as cursor:
        cursor.execute("DELETE FROM sessions WHERE last_time < NOW() - INTERVAL 10 MINUTE")
        cursor.connection.commit()


def set_room_starting_status(room_id: int) -> None:
    with get_connection().cursor() as cursor:
        cursor.execute("UPDATE games_rooms SET is_started = 1 WHERE id = %s", room_id)
        cursor.connection.commit()


def get_now_painter(room_id: int) -> int:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT now_painter FROM games_rooms WHERE id = %s", room_id)
        data = cursor.fetchone()
        if data is None:
            return -1
        else:
            return data[0]


def check_variant(variant: str, room_id: int) -> bool:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT now_word FROM games_rooms WHERE id = %s AND now_word = %s",
                       (room_id, variant.lower().strip()))
        data = cursor.fetchone()
        return data is not None


def get_room_word(room_id: int) -> str:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT now_word FROM games_rooms WHERE id = %s", room_id)
        data = cursor.fetchone()
        if data is None:
            return ""
        else:
            return data[0]


def is_painter_last(room_id: int) -> bool:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT now_painter FROM games_rooms WHERE id = %s AND now_painter=user_5", room_id)
        data = cursor.fetchone()
        if data is None:
            return False
        else:
            return True


def stop_room(room_id: int) -> None:
    with get_connection().cursor() as cursor:
        cursor.execute("UPDATE games_rooms SET is_started = 0 WHERE id = %s", room_id)
        cursor.connection.commit()


def next_painter(room_id: int) -> None:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT now_painter, user_1, user_2, user_3, user_4, user_5 FROM games_rooms WHERE id = %s",
                       room_id)
        data = cursor.fetchone()
        if data[0] == data[1]:
            new_painter = 2
        elif data[0] == data[2]:
            new_painter = 3
        elif data[0] == data[3]:
            new_painter = 4
        elif data[0] == data[4]:
            new_painter = 5
        else:
            return

        while new_painter <= 5:
            if data[new_painter] is not None:
                cursor.execute("UPDATE games_rooms SET now_painter = %s WHERE id = %s", (data[new_painter], room_id))
                new_painter = -1
                break
            new_painter += 1

        if new_painter == -1:
            cursor.connection.commit()
            stop_room(room_id)
            return

        cursor.execute("UPDATE games_rooms SET start_time = NOW() WHERE id = %s", room_id)

        cursor.connection.commit()


def is_room_started(room_id: int) -> int:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT is_started FROM games_rooms WHERE id = %s", room_id)
        data = cursor.fetchone()
        if data is None:
            return -1
        else:
            return 1 if data[0] else 0


def is_time_end_in_room(room_id: int) -> bool:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT 1 FROM games_rooms WHERE id = %s AND start_time < NOW() - INTERVAL 90 SECOND;", room_id)
        data = cursor.fetchone()
        if data is None:
            return False
        else:
            return True


def get_room_status_message(room_id: int) -> str:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT message FROM games_rooms WHERE id = %s", room_id)
        data = cursor.fetchone()
        if data is None:
            return ""
        else:
            return data[0]


def set_room_status_message(message: str, room_id: int) -> None:
    with get_connection().cursor() as cursor:
        cursor.execute("UPDATE games_rooms SET message = %s WHERE id = %s", (message, room_id))
        cursor.connection.commit()


def send_message(message: str, room_id: int) -> None:
    with get_connection().cursor() as cursor:
        cursor.execute("INSERT INTO messages (text, room_id) VALUES (%s, %s)", (message, room_id))
        cursor.connection.commit()


def check_room_for_freeze(room_id: int) -> str:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT 1 FROM games_rooms WHERE id = %s AND start_checked_time < NOW() - INTERVAL 10 SECOND",
                       room_id)
        data = cursor.fetchone()
        if data is None:
            return 'WAITING_CHECK'
        else:
            cursor.execute("UPDATE games_rooms SET start_checked_time = NULL WHERE id = %s", room_id)
            cursor.execute(
                'SELECT checked_user_1, checked_user_2, checked_user_3, checked_user_4, checked_user_5 FROM games_rooms WHERE id = %s',
                room_id)
            data = cursor.fetchone()
            if not data[0]:
                cursor.execute('UPDATE games_rooms SET user_1 = NULL WHERE id = %s', room_id)
            elif not data[1]:
                cursor.execute('UPDATE games_rooms SET user_2 = NULL WHERE id = %s', room_id)
            elif not data[2]:
                cursor.execute('UPDATE games_rooms SET user_3 = NULL WHERE id = %s', room_id)
            elif not data[3]:
                cursor.execute('UPDATE games_rooms SET user_4 = NULL WHERE id = %s', room_id)
            elif not data[4]:
                cursor.execute('UPDATE games_rooms SET user_5 = NULL WHERE id = %s', room_id)
            cursor.execute(
                'UPDATE games_rooms SET checked_user_1 = 0, checked_user_2 = 0, checked_user_3 = 0, checked_user_4 = 0, checked_user_5 = 0 WHERE id = %s',
                room_id)
            cursor.connection.commit()
            return 'WAITING'


def start_checked_started_room(room_id: int) -> None:
    with get_connection().cursor() as cursor:
        cursor.execute(
            "UPDATE games_rooms SET start_checked_time = NOW(), checked_user_1 = 0, checked_user_2 = 0, checked_user_3 = 0, checked_user_4 = 0, checked_user_5 = 0 WHERE id = %s",
            room_id)
        cursor.connection.commit()


def is_room_checked_time_end(room_id: int) -> bool:
    with get_connection().cursor() as cursor:
        cursor.execute("SELECT 1 FROM games_rooms WHERE id = %s AND start_checked_time < NOW() - INTERVAL 10 SECOND",
                       room_id)
        data = cursor.fetchone()
        if data is None:
            return False
        else:
            return True


def set_user_checked_for_room(room_id: int, user_id: int) -> None:
    with get_connection().cursor() as cursor:
        cursor.execute("UPDATE games_rooms SET checked_user_%s = 1 WHERE id = %s", (user_id, room_id))
        cursor.connection.commit()


def clean_room_for_freeze(room_id: int) -> None:
    with get_connection().cursor() as cursor:
        cursor.execute(
            'SELECT checked_user_1, checked_user_2, checked_user_3, checked_user_4, checked_user_5, user_1, user_2, user_3, user_4, user_5 FROM games_rooms WHERE id = %s',
            room_id)
        data = cursor.fetchone()
        user_deleted_list = []
        now_painter = get_now_painter(room_id)
        if not data[0]:
            cursor.execute('UPDATE games_rooms SET user_1 = NULL WHERE id = %s', room_id)
            user_deleted_list.append(1)
        elif not data[1]:
            cursor.execute('UPDATE games_rooms SET user_2 = NULL WHERE id = %s', room_id)
            user_deleted_list.append(2)
        elif not data[2]:
            cursor.execute('UPDATE games_rooms SET user_3 = NULL WHERE id = %s', room_id)
            user_deleted_list.append(3)
        elif not data[3]:
            cursor.execute('UPDATE games_rooms SET user_4 = NULL WHERE id = %s', room_id)
            user_deleted_list.append(4)
        elif not data[4]:
            cursor.execute('UPDATE games_rooms SET user_5 = NULL WHERE id = %s', room_id)
            user_deleted_list.append(5)
        cursor.connection.commit()
        user_deleted_list_id = [data[5 + i] for i in user_deleted_list]

        if now_painter in user_deleted_list_id:
            now_word = get_room_word(room_id)
            set_room_status_message('Игрок отключился. Правильный ответ: {}'.format(now_word), room_id)
            next_painter(room_id)

        if is_room_started(room_id):
            start_checked_started_room(room_id)
        cursor.connection.commit()


def check_game_check_user_state(room_id: int) -> bool:
    with get_connection().cursor() as cursor:
        cursor.execute(
            "SELECT id FROM games_rooms WHERE id = %s AND checked_user_1 = 1 AND checked_user_2 = 1 AND  checked_user_3 = 1 AND  checked_user_4 = 1 AND  checked_user_5 = 1 AND ",
            room_id)
        data = cursor.fetchone()
        if data is None:
            return False
        else:
            return True
