from services import db


# TODO: Make auto restart find for game, where some players don`t check room

def get_or_create_room(user_id: int) -> int:
    # TODO: Add check for user, that already has room
    buffer = db.get_new_of_free_room(user_id)
    db.close_now_connection()
    return buffer


def check_game_room_for_user(room_id: int, user_id: int) -> str:
    buffer = db.check_game_room_for_user(room_id, user_id)
    if buffer == 'STARTING':
        start_checked_for_game_game(room_id)
        if not db.game_room_set_user_checked(room_id, user_id):
            return ''
    elif buffer == 'WAITING_CHECK':
        if not db.game_room_set_user_checked(room_id, user_id):
            return ''
        if db.check_game_room(room_id):
            buffer = 'STARTED'
            start_game(room_id)
    db.close_now_connection()
    return buffer


def start_checked_for_game_game(room_id: int) -> None:
    db.start_checked_game(room_id)


def update_checked_for_game(room_id: int) -> None:
    db.check_game_room(room_id)


def check_game_check_user_state(room_id: int) -> bool:
    return db.check_game_check_user_state(room_id)


def start_game(room_id: int) -> None:
    db.set_drawer(room_id)
    db.set_room_starting_status(room_id)
    db.auto_set_room_word(room_id)


def get_role(room_id: int, user_id: int) -> str:
    painter_id = db.get_now_painter(room_id)
    db.close_now_connection()
    if user_id == painter_id:
        return 'PAINTER'
    else:
        return 'USER'


def get_messages(room_id: int) -> list[str]:
    data = db.get_messages_of_game(room_id)
    db.close_now_connection()
    return data


def next_drawer(room_id: int) -> None:
    if db.is_painter_last(room_id):
        db.stop_room(room_id)
        return

    db.next_painter(room_id)
    db.close_now_connection()


def try_variant(variant: str, room_id: int) -> bool:
    buffer = db.check_variant(variant, room_id)
    db.send_message(variant.lower().strip(), room_id)
    if buffer:
        next_drawer(room_id)
    db.close_now_connection()
    return buffer


def get_status(room_id: int) -> int:
    buffer = db.is_room_started(room_id)
    db.close_now_connection()
    return buffer


def get_now_painter(room_id: int) -> int:
    buffer = db.get_now_painter(room_id)
    db.close_now_connection()
    return buffer
