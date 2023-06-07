#!/bin/sh
source venv/bin/activate
cd app
exec gunicorn -b :5000 --access-logfile - --error-logfile - --workers 4 main:app