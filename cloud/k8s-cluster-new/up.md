## Подъем кластера

```shell
terraform init
terraform apply
```

[yandex console](https://console.yandex.cloud/folders/b1g2mc1nl38o7244uq5s)

## Работа с kubectl
1. Узнайте идентификатор k8s
2. Введите команду, поменяв идентификатор:
```shell
yc managed-kubernetes cluster \
   get-credentials <k8s-identificator> \
   --external --force
```

## K8S
### База + бэк
```shell
cd ../../compose/k8s
```

```shell
kubectl apply -f db
kubectl apply -f back
```

### Фронт
Дальше надо посмотреть адрес бэка из load balancer:
```shell
kubectl get services
```
И смотрим на external-api у back-lb.
swagger: http://<lb-ip>/swagger-ui/index.html

Вместо localhost:8080 надо вставить external ip балансировщика
```shell
cd /Users/lsaskov/Work/Programms/GitLab/lr-product-shop/front-react/lr-product-shop-app
docker buildx create --use
docker buildx build --build-arg NEXT_PUBLIC_BACKEND_URL=http://localhost:8080/ \
  --platform linux/amd64,linux/arm64 -t levs2001/lr-product-shop-front:0.0.2-pl --push .
```
Далее меняем контейнер у пода, при необходимости
```shell
cd /Users/lsaskov/Work/Programms/GitLab/lr-product-shop/compose/k8s/front/
# Надо поменять image: levs2001/lr-product-shop-app:0.0.2-pl на нужную версию в front-deployment.yaml 
```
Запускаем
```shell
kubectl apply -f front
```

Дальше надо включить прометеус и графану. Для этого переходим в cloud/grafana...
Там дока и нужные конфиги.
Также необходимо зааплоадить подготовленную борду (смотри json в папке).

## Подключение HPA
[kube doc](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale-walkthrough/)
```shell
kubectl autoscale deployment product-shop-back --cpu-percent=15 --min=1 --max=2
```
Проверяем статус hpa:
```shell
kubectl get hpa
```
Создаем нагрузку:
```shell
kubectl run -i --tty load-generator --rm --image=busybox:1.28 --restart=Never -- /bin/sh -c "while sleep 0.01; do wget -q -O- 'http://product-shop-back:8080/product-shop/search-product/?name=Ya%20product'; done"
```
Смотрим на нагрузку:
```shell
kubectl get hpa product-shop-back --watch
```

И смотрим в графану.


## Выключение кластера
```shell
cd /Users/lsaskov/Work/Programms/GitLab/lr-product-shop/cloud/k8s-cluster-new
terraform destroy
```
Также проверьте, что в консоли ничего не осталось.






## Допы
Если закончилось место для образов:
```shell
docker system prune --all --force
```
nginx
```shell
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml
kubectl get pods -n ingress-nginx
kubectl apply -f ingress.yml
kubectl describe ingress product-shop-front-ingress
kubectl get services -o wide -n ingress-nginx
```
В kubectl describe будет адрес nginx. Далее идем создавать api-gateway в yandex console.

```
kubectl exec --stdin --tty shell-demo -- /bin/bash
```

