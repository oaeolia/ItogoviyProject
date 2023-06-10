import os

DB_HOST = os.getenv('DB_HOST', 'localhost')
DB_USER_NAME = os.getenv('DB_USER_NAME', 'sketch_it_system')
DB_NAME = os.getenv('DB_NAME', 'sketch_it')
DB_PASSWORD = os.getenv('DB_PASSWORD', 'ske1ch_pass_!t_system_password')

API_URL_MAIN = '/api/v1'

RESPONSE_OK = 'OK'
RESPONSE_ERROR = 'ERROR'
RESPONSE_BAD = 'BAD'

TOKEN_LENGTH = 32
TOKEN_SYMBOLS = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'

UPLOAD_FOLDER = 'rooms/canvas'

ROUND_TIME = 270
ROUND_PAUSE_TIME = 10
