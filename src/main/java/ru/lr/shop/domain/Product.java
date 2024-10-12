package ru.lr.shop.domain;

/**
 * @param id - уникальный id продукта
 * @param name - название продукта
 * @param producer - производитель продукта
 * @param count - количество таких продуктов на складе
 */
public record Product(long id, String name, String producer, int count) {
    private static final Product NO_PRODUCT = new Product(-1, "", "", -1);

    public static Product noProduct() {
        return NO_PRODUCT;
    }

    public static Product of(long id, String name, String producer, int count) {
        return new Product(id, name, producer, count);
    }

    public boolean found() {
        return id != -1;
    }
}
