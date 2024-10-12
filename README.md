# Тестирование

Прежде чем запускать тест подними базы:

```shell
docker-compose -f ./src/test/resources/test_cluster/docker-compose.yml up
```

Если какие-то тесты закончились с ошибкой то лучше пересоздать контейнер
перед следующим запуском:

```shell
docker-compose -f ./src/test/resources/test_cluster/docker-compose.yml up --force-recreate  -V
```

Дождитесь пока кластер поднимется.
