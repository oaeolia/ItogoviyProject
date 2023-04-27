from flask import Flask, request, Response

app = Flask(__name__)


@app.route("/test/echo", methods=['POST', 'GET'])
def test_echo() -> Response:
    return Response(request.data)


if __name__ == '__main__':
    app.run(debug=True)
