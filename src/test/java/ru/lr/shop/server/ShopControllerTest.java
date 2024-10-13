package ru.lr.shop.server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;
import ru.lr.shop.Application;
import ru.lr.shop.domain.Product;
import ru.lr.shop.sys.TerminalUtils;

@SpringBootTest(properties = "spring.main.lazy-initialization=true", classes = Application.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestPropertySource(locations = {"classpath:test.properties"})
class ShopControllerTest {
    private final IShopController controller;

    ShopControllerTest(IShopController controller) {
        this.controller = controller;
    }

    @BeforeAll
    public static void setUp() throws IOException {
        TerminalUtils.executeDetach(
            "docker-compose", "-f", "./src/test/resources/test_cluster/docker-compose.yml", "up", "--force-recreate", "-V", "-d"
        );
    }

    @AfterAll
    public static void stop() throws IOException {
        TerminalUtils.executeDetach(
            "docker-compose", "-f", "./src/test/resources/test_cluster/docker-compose.yml", "down"
        );
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
        assertEquals(Set.of(productsWithSameName), Set.of(resp.getBody()));
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