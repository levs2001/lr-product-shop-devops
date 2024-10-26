from json import JSONDecodeError

import flet as ft
import requests
import json

class Item:
    def __init__(self, id, name, producer, count):
        self.id = id
        self.name = name
        self.producer = producer
        self.count = count


def request_get_id(id):
    url = f'http://localhost:8080/product-shop/get-product/?id={id}'
    headers = {
        'accept': '*/*'
    }

    response = requests.get(url, headers=headers)

    print('Status Code:', response.status_code)
    print('Response Body:', response.text)

    return response

def get_all(n):
    items = []
    for i in range(n):
        try:
            res = request_get_id(i).json()
            items.append(Item(res['id'], res['name'], res['producer'], res['count']))
        except JSONDecodeError:
            continue
    return items

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

def request_put(id, count):
    url = f'http://localhost:8080/product-shop/update-product-count/?id={id}&count={count}'
    headers = {
        'accept': '*/*'
    }

    response = requests.put(url, headers=headers)

    print('Status Code:', response.status_code)
    print('Response Body:', response.text)
    print('update')
    print(url)

def request_delete(id):
    url = f'http://localhost:8080/product-shop/delete-product?id={id}'
    headers = {
        'accept': '*/*'
    }

    response = requests.delete(url, headers=headers)

    print('Status Code:', response.status_code)
    print('Response Body:', response.text)

def main(page: ft.Page):
    page.title = "Item Management"
    page.theme_mode = "light"
    items = get_all(100)
    items_per_page = 7
    current_page = 0
    filtered_items = items

    def update_items_list():
        items_table.rows.clear()
        start_index = current_page * items_per_page
        end_index = start_index + items_per_page
        for item in filtered_items[start_index:end_index]:
            items_table.rows.append(
                ft.DataRow(
                    cells=[
                        ft.DataCell(ft.Text(item.id, size=20)),
                        ft.DataCell(ft.Text(item.name, size=20)),
                        ft.DataCell(ft.Text(item.producer, size=20)),
                        ft.DataCell(ft.Text(item.count, size=20)),
                        ft.DataCell(
                            ft.Row(
                                [
                                    ft.IconButton(icon=ft.icons.DELETE, on_click=lambda e, i=item: delete_item(i)),
                                    #ft.IconButton(icon=ft.icons.EDIT, on_click=lambda e, i=item: edit_item(i)),
                                    ft.IconButton(icon=ft.icons.UPDATE, on_click=lambda e, i=item: update_item(i)),
                                ]
                            )
                        ),
                    ]
                )
            )
        page.update()

    def add_item(e):
        id = id_input.value
        name = name_input.value
        producer = producer_input.value
        count = count_input.value
        if id and name and producer and count:
            request_post(id, name, producer, count)
            new_item = Item(id, name, producer, count)
            items.append(new_item)
            filtered_items = items  # Обновляем отфильтрованные элементы
            update_items_list()
            clear_inputs()

    def delete_item(item):
        request_delete(item.id)
        items.remove(item)
        filtered_items = items  # Обновляем отфильтрованные элементы
        update_items_list()

    def edit_item(item):
        id_input.value = item.id
        name_input.value = item.name
        producer_input.value = item.producer
        count_input.value = item.count
        page.update()

    def update_item(item):
        id_input.value = item.id
        name_input.value = item.name
        producer_input.value = item.producer
        count_input.value = item.count
        page.update()

    def confirm_update(e):
        id = id_input.value
        count = count_input.value
        if id and count:
            request_put(id, count)
            for item in items:
                if item.id == id:
                    item.count = count
            update_items_list()
            clear_inputs()

    def search_item(e):
        nonlocal filtered_items
        nonlocal current_page
        search_id = search_id_input.value
        search_name = search_name_input.value
        filtered_items = [item for item in items if (not search_id or str(item.id) == str(search_id)) and (
                not search_name or search_name.lower() in item.name.lower())]
        current_page = 0  # Сбрасываем текущую страницу на первую
        update_items_list()

    def clear_inputs():
        id_input.value = ""
        name_input.value = ""
        producer_input.value = ""
        count_input.value = ""
        page.update()

    def prev_page(e):
        nonlocal current_page
        if current_page > 0:
            current_page -= 1
            update_items_list()

    def next_page(e):
        nonlocal current_page
        if (current_page + 1) * items_per_page < len(filtered_items):
            current_page += 1
            update_items_list()

    def last_page(e):
        nonlocal current_page
        current_page = (len(filtered_items) - 1) // items_per_page
        update_items_list()

    # Поля ввода
    id_input = ft.TextField(label="ID")
    name_input = ft.TextField(label="Name")
    producer_input = ft.TextField(label="Producer")
    count_input = ft.TextField(label="Count")

    # Поля поиска
    search_id_input = ft.TextField(label="Search by ID")
    search_name_input = ft.TextField(label="Search by Name")

    # Кнопки
    add_button = ft.ElevatedButton(text="Add Item", on_click=add_item)
    search_button = ft.ElevatedButton(text="Search", on_click=search_item)
    prev_button = ft.ElevatedButton(text="Previous", on_click=prev_page)
    next_button = ft.ElevatedButton(text="Next", on_click=next_page)
    last_button = ft.ElevatedButton(text="Last", on_click=last_page)
    update_button = ft.ElevatedButton(text="Update Item", on_click=confirm_update)

    # Таблица элементов
    items_table = ft.DataTable(
        columns=[
            ft.DataColumn(ft.Text("ID", style=ft.TextThemeStyle.HEADLINE_MEDIUM)),
            ft.DataColumn(ft.Text("Name", style=ft.TextThemeStyle.HEADLINE_MEDIUM)),
            ft.DataColumn(ft.Text("Producer", style=ft.TextThemeStyle.HEADLINE_MEDIUM)),
            ft.DataColumn(ft.Text("Count", style=ft.TextThemeStyle.HEADLINE_MEDIUM)),
            ft.DataColumn(ft.Text("Actions", style=ft.TextThemeStyle.HEADLINE_MEDIUM)),
        ],
        rows=[],
    )

    # Основной контейнер
    page.add(
        ft.Column(
            [
                ft.Container(ft.Text("Продуктовый магазин", size=50, weight="bold"), padding=10,
                             alignment=ft.alignment.center),
                ft.Container(ft.Row([id_input, name_input, producer_input, count_input, add_button, update_button]),
                             padding=ft.padding.only(left=20)),
                ft.Container(ft.Text("Поиск", size=35, weight="bold"), padding=ft.padding.only(left=20)),
                ft.Container(ft.Row([search_id_input, search_name_input, search_button]),
                             padding=ft.padding.only(left=20)),
                ft.Container(items_table, padding=ft.padding.only(left=20), width=1400),
                ft.Container(ft.Row([prev_button, next_button, last_button]), padding=ft.padding.only(left=20)),
            ],
            spacing=25,
            scroll=True,  # Включаем прокрутку
        )
    )

    update_items_list()

if __name__ == '__main__':
    ft.app(target=main, view=ft.AppView.WEB_BROWSER, host='0.0.0.0', port=8001)

