FROM python:3.11

WORKDIR /home/root

COPY requirements.txt requirements.txt
RUN python -m venv venv
RUN venv/bin/pip install -r requirements.txt
RUN venv/bin/pip install gunicorn

COPY . app
COPY boot.sh ./
RUN chmod +x boot.sh

EXPOSE 5000
ENTRYPOINT ["/bin/bash", "./boot.sh"]