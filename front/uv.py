import flet as ft
import flet_fastapi

async def main(page: ft.Page):
    await page.add_async(
        ft.Text("Hello, Flet!")
    )

app = flet_fastapi.app(main)