import os

import settings
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
        buffer = db.check_room_for_freeze(room_id)
        if buffer != 'WAITING_CHECK':
            db.close_now_connection()
            return buffer
        if not db.game_room_set_user_checked(room_id, user_id):
            return ''
        if db.check_game_room(room_id):
            buffer = 'STARTED'
            start_game(room_id)
    db.close_now_connection()
    return buffer


def get_remaining_time(room_id: int) -> int:
    buffer = db.get_remaining_time(room_id)
    db.close_now_connection()
    return buffer


def start_checked_for_game_game(room_id: int) -> None:
    db.start_checked_game(room_id)


def check_game_for_freeze_users(room_id: int) -> None:
    if db.is_room_started(room_id) != 1:
        return

    if not db.is_room_checked_time_end(room_id):
        return
    # TODO: Remove (ONLY FOR TEST!)
    # db.clean_room_for_freeze(room_id)


def update_checked_for_game(room_id: int) -> None:
    db.check_game_room(room_id)


def check_game_check_user_state(room_id: int) -> bool:
    return db.check_game_check_user_state(room_id)


def start_game(room_id: int) -> None:
    db.set_drawer(room_id)
    db.set_room_starting_status(room_id)
    db.auto_set_room_word(room_id)
    db.start_checked_started_room(room_id)


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


def update_wait_state(room_id: int) -> bool:
    if db.is_room_waiting_state_end(room_id):
        return next_drawer(room_id)


def start_wait_state(room_id: int) -> None:
    db.set_room_waiting_state(room_id)


def next_drawer(room_id: int) -> bool:
    if db.is_painter_last(room_id):
        db.stop_room(room_id)
        return True

    db.next_painter(room_id)
    db.auto_set_room_word(room_id)
    db.close_now_connection()
    return False


def try_variant(variant: str, room_id: int) -> bool:
    buffer = db.check_variant(variant, room_id)
    db.send_message(variant.lower().strip(), room_id)
    if buffer:
        db.set_room_status_message("Слово угадано!", room_id)
        # TODO: Remake this
        start_wait_state(room_id)
        # next_drawer(room_id)
        db.auto_set_room_word(room_id)
    db.close_now_connection()
    return buffer


def get_status(room_id: int, user_id: int) -> int:
    buffer = db.is_room_started(room_id)
    db.set_user_checked_for_room(room_id, user_id)
    check_game_for_freeze_users(room_id)
    print("START CHECK STATUS: ", buffer)
    if buffer == 1:
        print("CHECK ROOM TO WAITING STATE")
        is_waiting = db.is_not_room_freeze(room_id)
        print("IS WAITING: ", is_waiting)
        if is_waiting == 0:
            print("CHECK WAITING STATE")
            if update_wait_state(room_id):
                print("END AFTER WAITING")
                return -1
            else:
                print("RETURN WAITING STATE")
                return 1
        buffer += 1
    db.close_now_connection()
    if buffer == 2:
        if check_for_end_time(room_id):
            print("TIME ENDED")
            return -1
    return buffer


def get_room_status_message(room_id: int) -> str:
    message = db.get_room_status_message(room_id)
    db.close_now_connection()
    return message


def check_for_end_time(room_id: int) -> bool:
    buffer = db.is_time_end_in_room(room_id)
    if buffer:
        db.set_room_status_message("Время закончилось! Правильный ответ: " + db.get_room_word(room_id), room_id)
        # TODO: Remake this
        if db.is_painter_last(room_id):
            db.stop_room(room_id)
            return True
        start_wait_state(room_id)
        # buffer = next_drawer(room_id)
        return False
    return False


def get_now_painter(room_id: int) -> int:
    buffer = db.get_now_painter(room_id)
    db.close_now_connection()
    return buffer


def get_word(room_id: int, user_id: int) -> str:
    if get_now_painter(room_id) != user_id:
        return ''
    buffer = db.get_room_word(room_id)
    db.close_now_connection()
    return buffer


def send_canvas(room_id: int, user_id: int, canvas) -> None:
    if db.get_now_painter(room_id) != user_id:
        return

    with open(os.path.join(settings.UPLOAD_FOLDER, str(room_id) + '.png'), 'bw') as file:
        file.write(canvas)

    db.close_now_connection()
