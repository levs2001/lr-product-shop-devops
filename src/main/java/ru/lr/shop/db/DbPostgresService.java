package ru.lr.shop.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.lr.shop.domain.Product;

@Service
public class DbPostgresService implements IDbService {
    private static final Logger log = LoggerFactory.getLogger(DbPostgresService.class);
    private static final String POSTGRES_USER = "postgres";
    private static final String POSTGRES_PASSWORD = "example";

    private final String postgresUri;
    private final Properties properties = new Properties() {{
        setProperty("user", POSTGRES_USER);
        setProperty("password", POSTGRES_PASSWORD);
    }};

    public DbPostgresService(@Value("${postgres.uri}") String postgresUri) throws SQLException {
        this.postgresUri = postgresUri;
        var postgresDriver = new Driver();
        if (!postgresDriver.acceptsURL(postgresUri)) {
            log.error("Can't initialize postgres driver.");
            throw new SQLException("Can't initialize postgres driver.");
        }
        DriverManager.registerDriver(postgresDriver);
    }

    private Connection getConnection() throws SQLException {
        var connection = DriverManager.getConnection(postgresUri, properties);
        connection.setAutoCommit(false);
        return connection;
    }

    @Override
    public long addProduct(Product product) throws SQLException {
        return withTransaction(connection -> {
            var id = insertProduct(connection, product.name());
            connectCount(connection, id, product.count());
            connectProducer(connection, id, product.producer());
            return id;
        });
    }

    @Override
    public Product getProduct(long id) throws SQLException {
        return withTransaction(connection -> {
            var st = connection.prepareStatement("""
                SELECT
                    product.id AS product_id,
                    product.name AS product_name,
                    producer.name AS producer_name,
                    product_count.count AS product_count
                FROM product
                JOIN product_count ON product.id = product_count.product_id
                JOIN product_producer ON product.id = product_producer.product_id
                JOIN producer ON product_producer.producer_id = producer.id
                WHERE product.id = ?;
                """);
            st.setLong(1, id);
            var rows = st.executeQuery();
            if (rows.next()) {
                return new Product(
                    rows.getLong(1),
                    rows.getString(2),
                    rows.getString(3),
                    rows.getInt(4)
                );
            }

            return Product.noProduct();
        });
    }

    @Override
    public List<Product> searchProduct(String name) throws SQLException {
        return withTransaction(connection -> {
            var st = connection.prepareStatement("""
                SELECT
                    product.id AS product_id,
                    product.name AS product_name,
                    producer.name AS producer_name,
                    product_count.count AS product_count
                FROM product
                JOIN product_count ON product.id = product_count.product_id
                JOIN product_producer ON product.id = product_producer.product_id
                JOIN producer ON product_producer.producer_id = producer.id
                WHERE product.name = ?;
                """);
            st.setString(1, name);
            var rows = st.executeQuery();

            List<Product> res = new ArrayList<>();
            while (rows.next()) {
                res.add(
                    new Product(
                        rows.getLong(1),
                        rows.getString(2),
                        rows.getString(3),
                        rows.getInt(4)
                    )
                );
            }
            return res;
        });
    }

    @Override
    public void updateCount(long id, int count) throws SQLException {
        withTransaction(connection -> {
                var st = connection.prepareStatement("""
                    UPDATE product_count
                    SET count = ?
                    WHERE product_id = ?;
                    """);
                st.setInt(1, count);
                st.setLong(2, id);
                st.executeUpdate();
            }
        );
    }

    @Override
    public void deleteProduct(long id) throws SQLException {
        withTransaction(connection -> {
                deleteById(connection, "product_producer", "product_id", id);
                deleteById(connection, "product_count", "product_id", id);
                deleteById(connection, "product", "id", id);
            }
        );
    }

    private void deleteById(Connection connection, String table, String idColumn, long id) throws SQLException {
        var st = connection.prepareStatement(String.format("""
            DELETE FROM %s
            WHERE %s = ?;
            """, table, idColumn));
        st.setLong(1, id);
        st.execute();
    }

    private void withTransaction(DbCons function) throws SQLException {
        var connection = getConnection();
        try {
            function.apply(connection);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException("Exception during transaction.", e);
        } finally {
            connection.close();
        }
    }

    private <T> T withTransaction(DbFunc<T> function) throws SQLException {
        var connection = getConnection();
        try {
            var res = function.apply(connection);
            connection.commit();
            return res;
        } catch (Exception e) {
            connection.rollback();
            throw new SQLException("Exception during transaction.", e);
        } finally {
            connection.close();
        }
    }

    private static long insertProduct(Connection connection, String productName) throws SQLException {
        var st = connection.prepareStatement("INSERT INTO product (name) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
        st.setString(1, productName);
        executeInsert(st);
        return getInsertedId(st);
    }

    private static void connectCount(Connection connection, long productId, int count) throws SQLException {
        var st = connection.prepareStatement("INSERT INTO product_count (product_id, count) VALUES (?, ?);");
        st.setLong(1, productId);
        st.setInt(2, count);
        executeInsert(st);
    }

    private static void connectProducer(Connection connection, long productId, String producerName) throws SQLException {
        long producerId = createProducerIfNotExists(connection, producerName);
        var st = connection.prepareStatement("INSERT INTO product_producer (product_id, producer_id) VALUES (?, ?);");
        st.setLong(1, productId);
        st.setLong(2, producerId);
        executeInsert(st);
    }

    private static long createProducerIfNotExists(Connection connection, String producerName) throws SQLException {
        var getSt = connection.prepareStatement("SELECT id FROM producer WHERE name = ?;");
        getSt.setString(1, producerName);
        var existsProducer = getSt.executeQuery();
        if (existsProducer.next()) {
            return existsProducer.getLong(1);
        }

        var st = connection.prepareStatement("INSERT INTO producer (name) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
        st.setString(1, producerName);
        executeInsert(st);
        return getInsertedId(st);
    }

    /**
     * Works only with RETURN_GENERATED_KEYS statement.
     */
    private static long getInsertedId(Statement st) throws SQLException {
        try (var generatedKeys = st.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
        }
    }

    private static void executeInsert(PreparedStatement st) throws SQLException {
        var affectedRows = st.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Failed insert.");
        }
    }

    interface DbCons {
        void apply(Connection connection) throws SQLException;
    }

    interface DbFunc<T> {
        T apply(Connection connection) throws SQLException;
    }
}
