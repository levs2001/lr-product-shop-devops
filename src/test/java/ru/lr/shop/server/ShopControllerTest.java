package ru.lr.shop.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.lr.shop.alert.IAlertService;
import ru.lr.shop.domain.Product;

class ShopControllerTest {
    private static IShopController controller;

    @BeforeAll
    public static void setUp() throws IOException {
        controller = new ShopController(new DbServiceMock(), mock(IAlertService.class), 1000);
    }

    @Test
    public void testAddAndGet() {
        var expected = new Product(-1, "product1", "producer1", 10);
        var id = addWithCheck(expected);
        ResponseEntity<Product> answer = controller.getProduct(id);
        assertEquals(answer.getStatusCode(), HttpStatus.OK);
        assertEquals(new Product(id, expected.name(), expected.producer(), expected.count()), answer.getBody());
    }

    @Test
    public void testDelete() {
        var id = addWithCheck(new Product(-1, "product1", "producer1", 10));
        deleteWithCheck(id);
    }

    @Test
    public void testUpdateCount() {
        var init = new Product(-1, "product_c", "producer_c", 10);
        var id = addWithCheck(init);
        var expectedCount = 15;
        var resp = controller.updateCount(id, expectedCount);
        assertEquals(resp.getStatusCode(), HttpStatus.OK);
        var actualResp = controller.getProduct(id);
        assertEquals(actualResp.getStatusCode(), HttpStatus.OK);
        var expected = new Product(id, init.name(), init.producer(), expectedCount);
        assertEquals(expected, actualResp.getBody());
    }

    @Test
    public void testSearchByName() {
        var nameToSearch = "product_search_name";
        var expectedSearchCount = 10;
        var productsWithSameName = new ArrayList<Product>();
        for (int i = 0; i < expectedSearchCount; i++) {
            var product = Product.of(-1, nameToSearch, "producer_" + i, i);
            var id = addWithCheck(product);
            productsWithSameName.add(Product.of(id, product.name(), product.producer(), product.count()));
        }

        var anotherProductsCount = 25;
        for (int i = 0; i < anotherProductsCount; i++) {
            var namePostfix = i + 150 + ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
            var count = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
            addWithCheck(Product.of(-1, "name_" + namePostfix, "producer_" + i, count));
        }

        var resp = controller.searchProduct(nameToSearch);
        assertEquals(resp.getStatusCode(), HttpStatus.OK);
        assertNotNull(resp.getBody());
        assertEquals(Set.copyOf(productsWithSameName), Set.copyOf(resp.getBody()));
    }

    private long addWithCheck(Product product) {
        var response = controller.addProduct(product);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    public void deleteWithCheck(long id) {
        var status = controller.deleteProduct(id);
        assertEquals(HttpStatus.OK, status.getStatusCode());
        var ans = controller.getProduct(id);
        assertNull(ans.getBody());
        assertEquals(HttpStatus.NOT_FOUND, ans.getStatusCode());
    }
}