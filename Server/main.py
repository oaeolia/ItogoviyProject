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
        return Response(json.dumps({'status': settings.RESPONSE_OK, 'massage': 'Not all data in request'}))
    registration_message = auth.registration(data['name'], data['email'], data['password'])
    if registration_message == '':
        return Response(json.dumps({'status': settings.RESPONSE_OK}))
    else:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': registration_message}))


@app.route(settings.API_URL_MAIN + '/auth/login', methods=['POST'])
def login() -> Response:
    data = request.get_json()
    if 'login' not in data or 'password' not in data:
        return Response(json.dumps({'status': settings.RESPONSE_OK, 'massage': 'Not all data in request'}))

    login_message = auth.auth(data['login'], data['password'])
    print(type(login_message))
    if type(login_message) is not str:
        return Response(json.dumps({'status': settings.RESPONSE_OK, 'session_id': login_message[1], 'session_token': login_message[0]}))
    else:
        return Response(json.dumps({'status': settings.RESPONSE_ERROR, 'message': login_message}))


if __name__ == '__main__':
    app.run(debug=True)
