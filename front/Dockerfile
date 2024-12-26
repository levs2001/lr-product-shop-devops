FROM python:3.10

ENV PYTHONUNBUFFERED=1

RUN mkdir app
WORKDIR /app
COPY . .

RUN pip install -r ./requirements.txt
CMD ["python", "./ui.py"]





