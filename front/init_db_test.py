import requests
import json


def request_post(id, name, producer, count):
    url = 'http://localhost:8080/product-shop/add-product/'
    headers = {
        'accept': '*/*',
        'Content-Type': 'application/json'
    }
    data = {
        'id': id,
        'name': name,
        'producer': producer,
        'count': count
    }
    response = requests.post(url, headers=headers, data=json.dumps(data))
    print('Status Code:', response.status_code)
    print('Response Body:', response.text)


def generate_data(n):
    products = [
        'Молоко', 'Хлеб', 'Яблоки', 'Бананы', 'Мясо', 'Рыба', 'Сыр', 'Масло', 'Яйца', 'Сахар',
        'Соль', 'Кофе', 'Чай', 'Макароны', 'Рис', 'Картофель', 'Морковь', 'Лук', 'Помидоры', 'Огурцы'
    ]
    producers = [
        'Простоквашино', 'Хлебопекарня', 'Сады Придонья', 'Чикита', 'Мираторг', 'Вичуга', 'Вимм-Билль-Данн',
        'Президент', 'Маслодел', 'Овощной мир', 'Соль России', 'Нестле', 'Липтон', 'Макфа', 'Рис-Про',
        'Картофельный дом', 'Морковь-Про', 'Лук-Про', 'Помидор-Про', 'Огурец-Про'
    ]

    data = []
    for i in range(n):
        product = products[i % len(products)]
        producer = producers[i % len(producers)]
        data.append({
            'id': i + 1,
            'name': product,
            'producer': producer,
            'count': (i + 1) * 10
        })
    return data


def main():
    data = generate_data(100)
    for item in data:
        request_post(item['id'], item['name'], item['producer'], item['count'])


if __name__ == '__main__':
    main()
