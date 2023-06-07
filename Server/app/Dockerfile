#FROM ubuntu:22.04
#RUN apt-get update -y
#RUN apt-get install -y python3-pip python3-dev build-essential python3
#COPY . /app
#WORKDIR /app
#RUN pip install -r requirements.txt
#ENTRYPOINT ["python3"]
#CMD ["main.py"]

FROM python:3.11

#RUN adduser -D sketch_it_system

WORKDIR /home/root

COPY requirements.txt requirements.txt
RUN python -m venv venv
RUN venv/bin/pip install -r requirements.txt
RUN venv/bin/pip install gunicorn

COPY . app
COPY boot.sh ./
RUN chmod +x boot.sh


#RUN chown -R sketch_it_system:sketch_it_system ./
#USER sketch_it_system

EXPOSE 5000
#ENTRYPOINT ["./boot.sh"]
ENTRYPOINT ["/bin/bash", "./boot.sh"]