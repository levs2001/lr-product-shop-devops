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
```shell
kubectl apply -f ../../compose/k8s
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

## Выключение кластера
```shell
terraform destroy
```
Также проверьте, что в консоли ничего не осталось.
