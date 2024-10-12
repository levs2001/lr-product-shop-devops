package ru.lr.shop.db;

import java.sql.SQLException;
import java.util.List;
import ru.lr.shop.domain.Product;

public interface IDbService {
    long addProduct(Product product) throws SQLException;

    Product getProduct(long id) throws SQLException;

    List<Product> searchProduct(String name) throws SQLException;

    void updateCount(long id, int count) throws SQLException;

    void deleteProduct(long id) throws SQLException;
}
