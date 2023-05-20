import os

DB_HOST = os.getenv('DB_HOST', 'localhost')
DB_USER_NAME = os.getenv('DB_USER_NAME', 'root')
DB_NAME = os.getenv('DB_NAME', 'samsung_finally_project')
DB_PASSWORD = os.getenv('DB_PASSWORD', '')

API_URL_MAIN = '/api/v1'

RESPONSE_OK = 'OK'
RESPONSE_ERROR = 'ERROR'
RESPONSE_BAD = 'BAD'

TOKEN_LENGTH = 32
TOKEN_SYMBOLS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'

UPLOAD_FOLDER = 'rooms/canvas'
