FROM python:3.12

RUN mkdir app
WORKDIR /app
COPY ./front ./front

RUN pip install -r ./front/requirements.txt





