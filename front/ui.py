import os

import flet as ft
import requests
import json

BACKEND_URL = os.environ.get('BACKEND_URL', 'http://localhost:8080/')


def get_url(method: str) -> str:
    return BACKEND_URL + method


class Item:
    def __init__(self, id, name, producer, count):
        self.id = id
        self.name = name
        self.producer = producer
        self.count = count


def safe_request(req_func, name):
    try:
        return req_func()
    except Exception as error:
        print(f"Error with request {name}: ", error)


def request_get_id(id):
    url = get_url(f'product-shop/get-product/?id={id}')
    headers = {
        'accept': '*/*'
    }
    req_func = lambda: requests.get(url, headers=headers)
    response = safe_request(req_func, "get_id")

    print('Status Code:', response.status_code)
    print('Response Body:', response.text)

    return response


def get_all(n):
    items = []
    for i in range(n):
        try:
            res = request_get_id(i).json()
            items.append(Item(res['id'], res['name'], res['producer'], res['count']))
        except Exception as error:
            print(f"Error while getting element {i}: {error}")
            continue
    return items


def request_post(id, name, producer, count):
    url = get_url('product-shop/add-product/')
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
    req_func = lambda: requests.post(url, headers=headers, data=json.dumps(data))
    response = safe_request(req_func, "post")
    print('Status Code:', response.status_code)
    print('Response Body:', response.text)


def request_put(id, count):
    url = get_url(f'product-shop/update-product-count/?id={id}&count={count}')
    headers = {
        'accept': '*/*'
    }

    req_func = lambda: requests.put(url, headers=headers)
    response = safe_request(req_func, "put")
    print('Status Code:', response.status_code)
    print('Response Body:', response.text)
    print('update')
    print(url)


def request_delete(id):
    url = get_url(f'product-shop/delete-product?id={id}')
    headers = {
        'accept': '*/*'
    }

    req_func = lambda: requests.delete(url, headers=headers)
    response = safe_request(req_func, "delete")
    print('Status Code:', response.status_code)
    print('Response Body:', response.text)


def main(page: ft.Page):
    print("Starting page...")
    print("Backend url: ", BACKEND_URL)

    page.title = "Item Management"
    page.theme_mode = "light"
    items = get_all(100)
    items_per_page = 7
    current_page = 0
    filtered_items = items

    def update_items():
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
                                    # ft.IconButton(icon=ft.icons.EDIT, on_click=lambda e, i=item: edit_item(i)),
                                    ft.IconButton(icon=ft.icons.UPDATE, on_click=lambda e, i=item: update_item(i)),
                                ]
                            )
                        ),
                    ]
                )
            )

    def update_all():
        print("Updating page...")
        try:
            update_items()
        except Exception as error:
            print("Error during items update: ", error)
        page.update()
        print("Updating page finished.")

    def add_item(e):
        print("Add item req.")
        id = id_input.value
        name = name_input.value
        producer = producer_input.value
        count = count_input.value
        if id and name and producer and count:
            request_post(id, name, producer, count)
            new_item = Item(id, name, producer, count)
            items.append(new_item)
            filtered_items = items  # Обновляем отфильтрованные элементы
            update_all()
            clear_inputs()

    def delete_item(item):
        print("Delete item req.")
        request_delete(item.id)
        items.remove(item)
        filtered_items = items  # Обновляем отфильтрованные элементы
        update_all()

    def edit_item(item):
        print("Edit item req")
        id_input.value = item.id
        name_input.value = item.name
        producer_input.value = item.producer
        count_input.value = item.count
        page.update()

    def update_item(item):
        print("Update item req")
        id_input.value = item.id
        name_input.value = item.name
        producer_input.value = item.producer
        count_input.value = item.count
        page.update()

    def confirm_update(e):
        print("Confirm update req.")
        id = id_input.value
        count = count_input.value
        if id and count:
            request_put(id, count)
            for item in items:
                if item.id == id:
                    item.count = count
            update_all()
            clear_inputs()

    def search_item(e):
        print("Search item req.")
        nonlocal filtered_items
        nonlocal current_page
        search_id = search_id_input.value
        search_name = search_name_input.value
        filtered_items = [item for item in items if (not search_id or str(item.id) == str(search_id)) and (
                not search_name or search_name.lower() in item.name.lower())]
        current_page = 0  # Сбрасываем текущую страницу на первую
        update_all()

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
            update_all()

    def next_page(e):
        nonlocal current_page
        if (current_page + 1) * items_per_page < len(filtered_items):
            current_page += 1
            update_all()

    def last_page(e):
        nonlocal current_page
        current_page = (len(filtered_items) - 1) // items_per_page
        update_all()

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

    update_all()
    print("Page initialization finished.")


if __name__ == '__main__':
    print("App initialiazation")
    ft.app(target=main, view=ft.AppView.WEB_BROWSER, host='0.0.0.0', port=8001)
    print("Finished app initialiazation")
