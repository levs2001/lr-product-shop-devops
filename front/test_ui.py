import unittest
from unittest.mock import patch, MagicMock
import requests
from json import JSONDecodeError

from ui import request_get_id, get_all, request_post, request_put, request_delete, Item

class TestItemManagement(unittest.TestCase):

    @patch('requests.get')
    def test_request_get_id(self, mock_get):
        # Имитация ответа от сервера
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {
            'id': 1,
            'name': 'Test Item',
            'producer': 'Test Producer',
            'count': 10
        }
        mock_get.return_value = mock_response

        # Вызов функции
        response = request_get_id(1)

        # Проверка вызова requests.get
        mock_get.assert_called_once_with('http://localhost:8080/product-shop/get-product/?id=1', headers={'accept': '*/*'})

        # Проверка ответа
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {
            'id': 1,
            'name': 'Test Item',
            'producer': 'Test Producer',
            'count': 10
        })

    @patch('ui.request_get_id')
    def test_get_all(self, mock_request_get_id):
        mock_response = MagicMock()
        mock_response.json.return_value = {
            'id': 1,
            'name': 'Test Item',
            'producer': 'Test Producer',
            'count': 10
        }
        mock_request_get_id.return_value = mock_response

        items = get_all(1)

        mock_request_get_id.assert_called_once_with(0)

        self.assertEqual(len(items), 1)
        self.assertEqual(items[0].id, 1)
        self.assertEqual(items[0].name, 'Test Item')
        self.assertEqual(items[0].producer, 'Test Producer')
        self.assertEqual(items[0].count, 10)

    @patch('requests.post')
    def test_request_post(self, mock_post):
        mock_response = MagicMock()
        mock_response.status_code = 201
        mock_post.return_value = mock_response

        request_post(1, 'New Item', 'New Producer', 50)

        mock_post.assert_called_once_with(
            'http://localhost:8080/product-shop/add-product/',
            headers={'accept': '*/*', 'Content-Type': 'application/json'},
            data='{"id": 1, "name": "New Item", "producer": "New Producer", "count": 50}'
        )

        self.assertEqual(mock_response.status_code, 201)

    @patch('requests.put')
    def test_request_put(self, mock_put):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_put.return_value = mock_response

        request_put(1, 50)

        mock_put.assert_called_once_with(
            'http://localhost:8080/product-shop/update-product-count/?id=1&count=50',
            headers={'accept': '*/*'}
        )

        self.assertEqual(mock_response.status_code, 200)

    @patch('requests.delete')
    def test_request_delete(self, mock_delete):
        mock_response = MagicMock()
        mock_response.status_code = 204
        mock_delete.return_value = mock_response

        request_delete(1)

        mock_delete.assert_called_once_with(
            'http://localhost:8080/product-shop/delete-product?id=1',
            headers={'accept': '*/*'}
        )

        self.assertEqual(mock_response.status_code, 204)

if __name__ == '__main__':
    unittest.main()
