import json

from flask import Flask, request, Response

import settings
from services import db, auth

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
            return Response(json.dumps({'status': settings.RESPONSE_OK, 'session_id': login_message[1], 'session_token': login_message[0]}))
        else:
            return Response(json.dumps({'status': settings.RESPONSE_BAD, 'message': login_message}))
    else:
        login_message = auth.auth(data['login'], data['password'])
        if type(login_message) is not str:
            return Response(json.dumps({'status': settings.RESPONSE_OK, 'session_id': login_message[1], 'session_token': login_message[0], 'application_token': login_message[2], 'application_id': login_message[3]}))
        else:
            return Response(json.dumps({'status': settings.RESPONSE_BAD, 'message': login_message}))


@app.route(settings.API_URL_MAIN + '/auth/logout', methods=['POST'])
def logout() -> Response:
    data = request.get_json()
    if 'application_session_id' not in data or 'application_session_token' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': 'Not all data in request'}))
    auth.logout_from_application(data['application_session_id'], data['application_session_token'])
    return Response(json.dumps({'status': settings.RESPONSE_OK}))


@app.route(settings.API_URL_MAIN + '/tools/clear_sessions', methods=['POST'])
def clear_sessions():
    auth.clear_sessions()


if __name__ == '__main__':
    app.run(debug=True, host='192.168.1.13')
