package ru.lr.shop.server;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.lr.shop.alert.Alert;
import ru.lr.shop.alert.IAlertService;
import ru.lr.shop.db.IDbService;
import ru.lr.shop.domain.Product;

@RestController
public class ShopController implements IShopController {
    private static final Logger log = LoggerFactory.getLogger(ShopController.class);
    private final IDbService db;
    private final IAlertService alertService;
    private final int countThreshold;

    public ShopController(IDbService db, IAlertService alertService, @Value("${count.threshold}") int countThreshold) {
        this.db = db;
        this.alertService = alertService;
        this.countThreshold = countThreshold;
    }

    @Override
    public ResponseEntity<Long> addProduct(Product product) {
        log.info("Adding product: {}", product);
        try {
            var id = db.addProduct(product);
            if (product.count() > countThreshold) {
                alertService.makeAlert(String.format("Alert: Product %s have too big count %s", id, product.count()));
            }
            return new ResponseEntity<>(id, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error during adding product", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<Product> getProduct(long id) {
        log.info("Getting product with id: {}", id);
        try {
            var product = db.getProduct(id);
            if (product.found()) {
                return ResponseEntity.ok(product);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error during getting product {}.", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<List<Product>> searchProduct(String name) {
        log.info("Searching product with name: {}", name);
        try {
            var productList = db.searchProduct(name);
            return ResponseEntity.ok(productList);
        } catch (Exception e) {
            log.error("Error during searching product by name {}.", name, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<String> updateCount(long id, int count) {
        log.info("Updating product with {} to count {}", id, count);
        try {
            db.updateCount(id, count);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error during updating product count, id: {}.", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<String> deleteProduct(long id) {
        log.info("Deleting product: {}", id);
        try {
            db.deleteProduct(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error during deleting product, id: {}.", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<List<Alert>> getAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @Override
    public ResponseEntity<String> health() {
        return ResponseEntity.ok().build();
    }
}
