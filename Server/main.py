import json
import os

from flask import Flask, request, Response

import settings
from services import db, auth, game

app = Flask(__name__)


@app.route('/test/echo', methods=['POST', 'GET'])
def test_echo() -> Response:
    return Response(request.data)


@app.route('/test/db', methods=['POST', 'GET'])
def test_db() -> Response:
    try:
        db.test_connect_db()
        return Response('OK')
    except Exception as e:
        return Response(str(e))


@app.route(settings.API_URL_MAIN + '/auth/registration', methods=['POST'])
def registration() -> Response:
    data = request.get_json()
    if 'name' not in data or 'email' not in data or 'password' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))
    registration_message = auth.registration(data['name'], data['email'], data['password'])
    if registration_message == '':
        return Response(json.dumps({'status': settings.RESPONSE_OK}))
    else:
        return Response(json.dumps({'status': settings.RESPONSE_BAD, 'message': registration_message}))


@app.route(settings.API_URL_MAIN + '/auth/login', methods=['POST'])
def login() -> Response:
    data = request.get_json()
    if ('login' not in data or 'password' not in data) and ('application_token' not in data or 'application_session_id' not in data):
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))

    if 'application_token' in data:
        login_message = auth.auth_application(data['application_token'], data['application_session_id'])
        if type(login_message) is not str:
            return Response(json.dumps({'status': settings.RESPONSE_OK, 'session_id': login_message[1], 'session_token': login_message[0], 'user_id': login_message[2]}))
        else:
            return Response(json.dumps({'status': settings.RESPONSE_BAD, 'message': login_message}))
    else:
        login_message = auth.auth(data['login'], data['password'])
        if type(login_message) is not str:
            return Response(json.dumps({'status': settings.RESPONSE_OK,
                                        'session_id': login_message[1],
                                        'session_token': login_message[0],
                                        'application_token': login_message[2],
                                        'application_id': login_message[3],
                                        'user_id': login_message[4]}))
        else:
            return Response(json.dumps({'status': settings.RESPONSE_BAD, 'message': login_message}))


@app.route(settings.API_URL_MAIN + '/auth/logout', methods=['POST'])
def logout() -> Response:
    data = request.get_json()
    if 'application_session_id' not in data or 'application_session_token' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))
    auth.logout_from_application(data['application_session_id'], data['application_session_token'])
    return Response(json.dumps({'status': settings.RESPONSE_OK}))


@app.route(settings.API_URL_MAIN + '/game/get_new_room', methods=['POST'])
def get_new_room() -> Response:
    data = request.get_json()
    if 'session_id' not in data or 'session_token' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))
    session = auth.get_session(data['session_id'], data['session_token'])
    if session is None:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Invalid session'}))
    return Response(json.dumps({'status': settings.RESPONSE_OK, 'room_id': game.get_or_create_room(session['user_id'])}))


@app.route(settings.API_URL_MAIN + '/game/check_room', methods=['POST'])
def user_check_room() -> Response:
    data = request.get_json()
    if 'session_id' not in data or 'session_token' not in data or 'room_id' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))
    session = auth.get_session(data['session_id'], data['session_token'])
    if session is None:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Invalid session'}))
    status = game.check_game_room_for_user(data['room_id'], session['user_id'])
    if status == '':
        return Response(json.dumps({'status': settings.RESPONSE_ERROR}))
    return Response(json.dumps({'status': settings.RESPONSE_OK, 'room_status': status}))


@app.route(settings.API_URL_MAIN + '/game/get_role', methods=['POST'])
def get_role() -> Response:
    data = request.get_json()
    if 'session_id' not in data or 'session_token' not in data or 'room_id' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))
    session = auth.get_session(data['session_id'], data['session_token'])
    if session is None:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Invalid session'}))

    try:
        return Response(json.dumps({'status': settings.RESPONSE_OK, 'role': game.get_role(data['room_id'], session['user_id']), 'painter': game.get_now_painter(data['room_id'])}))
    except Exception as e:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': str(e)}))


@app.route(settings.API_URL_MAIN + '/game/get_messages', methods=['POST'])
def get_messages() -> Response:
    data = request.get_json()
    if 'session_id' not in data or 'session_token' not in data or 'room_id' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))
    session = auth.get_session(data['session_id'], data['session_token'])
    if session is None:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Invalid session'}))
    return Response(json.dumps({'status': settings.RESPONSE_OK, 'messages': game.get_messages(data['room_id'])}))


@app.route(settings.API_URL_MAIN + '/game/try_variant', methods=['POST'])
def try_variant() -> Response:
    data = request.get_json()
    if 'session_id' not in data or 'session_token' not in data or 'room_id' not in data or 'variant' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))
    session = auth.get_session(data['session_id'], data['session_token'])
    if session is None:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Invalid session'}))
    return Response(json.dumps({'status': settings.RESPONSE_OK, 'result': game.try_variant(data['variant'], data['room_id'])}))


@app.route(settings.API_URL_MAIN + '/game/get_status', methods=['POST'])
def get_status() -> Response:
    data = request.get_json()
    if 'session_id' not in data or 'session_token' not in data or 'room_id' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))

    session = auth.get_session(data['session_id'], data['session_token'])

    if session is None:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Invalid session'}))

    status = game.get_status(data['room_id'])

    if status == 'END':
        return Response(json.dumps({'status': settings.RESPONSE_OK, 'game_status': status}))

    now_painter = game.get_now_painter(data['room_id'])
    message = game.get_room_status_message(data['room_id'])
    return Response(json.dumps({'status': settings.RESPONSE_OK, 'game_status': status, 'now_painter': now_painter, 'message': message}))


@app.route(settings.API_URL_MAIN + '/game/send_canvas', methods=['POST'])
def send_canvas() -> Response:
    data = request.headers
    if 'Session-Id' not in data or 'Session-Token' not in data or 'Room-Id' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))
    session = auth.get_session(int(data['session_id']), data['session_token'])
    game.send_canvas(int(data['room_id']), session['user_id'], request.data)
    return Response(json.dumps({'status': settings.RESPONSE_OK}))


@app.route(settings.API_URL_MAIN + '/game/get_word', methods=['POST'])
def get_word() -> Response:
    data = request.get_json()
    if 'session_id' not in data or 'session_token' not in data or 'room_id' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))
    session = auth.get_session(data['session_id'], data['session_token'])
    if session is None:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Invalid session'}))
    word = game.get_word(data['room_id'], session['user_id'])
    if word == '':
        return Response(json.dumps({'status': settings.RESPONSE_BAD}))
    else:
        return Response(json.dumps({'status': settings.RESPONSE_OK, 'word': word}))


@app.route(settings.API_URL_MAIN + '/game/get_canvas', methods=['POST'])
def get_canvas() -> Response:
    data = request.get_json()
    if 'session_id' not in data or 'session_token' not in data or 'room_id' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))
    session = auth.get_session(data['session_id'], data['session_token'])
    if session is None:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Invalid session'}))

    with open(os.path.join(settings.UPLOAD_FOLDER, str(data['room_id']) + '.png'), 'rb') as file:
        file_data = file.read()

    return Response(bytes(file_data), mimetype='image/png')


@app.route(settings.API_URL_MAIN + '/tools/clear_sessions', methods=['POST'])
def clear_sessions():
    auth.clear_sessions()
    return Response(json.dumps({'status': settings.RESPONSE_OK}))


if __name__ == '__main__':
    app.run(debug=True, host='192.168.1.13')
