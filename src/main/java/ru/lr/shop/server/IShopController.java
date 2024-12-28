package ru.lr.shop.server;


import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.lr.shop.alert.Alert;
import ru.lr.shop.domain.Product;

@RequestMapping("/product-shop") @CrossOrigin(origins = "*")
public interface IShopController {
    /**
     * @param product, обратите внимание, что при добавлении id игнорируется, метод вернет id созданного продукта сам.
     * @return id добавленного продукта
     */
    @PostMapping("add-product/")
    ResponseEntity<Long> addProduct(@RequestBody Product product);

    @GetMapping("get-product/")
    ResponseEntity<Product> getProduct(@RequestParam long id);

    @GetMapping("search-product/")
    ResponseEntity<List<Product>> searchProduct(@RequestParam String name);

    @PutMapping("update-product-count/")
    ResponseEntity<String> updateCount(@RequestParam long id, @RequestParam int count);

    @DeleteMapping("delete-product")
    ResponseEntity<String> deleteProduct(long id);

    @GetMapping("get-alerts/")
    ResponseEntity<List<Alert>> getAlerts();

    @GetMapping("health")
    ResponseEntity<String> health();
}
