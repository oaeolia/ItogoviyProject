from services import db


# TODO: Make auto restart find for game, where some players dont check room

def get_or_create_room(user_id: int) -> int:
    # TODO: Add check for user, that already has room
    buffer = db.get_new_of_free_room(user_id)
    db.close_now_connection()
    return buffer


def check_game_room_for_user(user_id: int) -> str:
    buffer = db.check_game_room_for_user(user_id)
    if buffer == 'STARTING':
        room_id = db.get_user_room_id(user_id)
        start_checked_for_game_game(room_id)
        db.game_room_set_user_checked(user_id)
    elif buffer == 'WAITING_CHECK':
        room_id = db.get_user_room_id(user_id)
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
    db.set_room_starting_status(room_id)
    db.auto_set_room_word(room_id)
