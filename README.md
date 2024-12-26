# Использование
Для использования и запуска тестов должен быть запущен докер.

Обратите внимание, клиент сам создаст все нужные таблицы и индексы в базах.
Все взаимодействие должно происходить только через клиент.

Для документации и примеров запросов развернут swagger, если приложение запущено,
он доступен по ссылке: http://localhost:8080/swagger-ui/index.html

# Тестирование
При тестах автоматически запускается скрипт с поднятием тестового кластера:
```shell
docker-compose -f ./src/test/resources/test_cluster/docker-compose.yml up --force-recreate  -V
```

```commandline
docker buildx create --use

docker buildx build --platform linux/amd64,linux/arm64 -t levs2001/lr-product-shop-front:0.0.8-pl --push .
```
