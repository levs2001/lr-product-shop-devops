package ru.lr.shop.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import ru.lr.shop.db.IDbService;
import ru.lr.shop.domain.Product;

public class DbServiceMock implements IDbService {
    private final Map<Long, Product> products = new HashMap<>();

    @Override
    public long addProduct(Product product) {
        var id = getRandomId();
        products.put(id, Product.of(id, product.name(), product.producer(), product.count()));
        return id;
    }

    @Override
    public Product getProduct(long id) {
        var res = products.get(id);
        if (res == null) {
            return Product.noProduct();
        }
        return res;
    }

    @Override
    public List<Product> searchProduct(String name) {
        List<Product> res = new ArrayList<>();
        for (var p : products.values()) {
            if (p.name().equals(name)) {
                res.add(p);
            }
        }
        return res;
    }

    @Override
    public void updateCount(long id, int count) {
        products.computeIfPresent(id, (k, p) -> Product.of(id, p.name(), p.producer(), count));
    }

    @Override
    public void deleteProduct(long id) {
        products.remove(id);
    }

    private long getRandomId() {
        return ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
    }
}
