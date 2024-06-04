package org.iqkv.boutique.repository.rowmapper;

import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.util.function.BiFunction;
import org.iqkv.boutique.domain.ProductOrder;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link ProductOrder}, with proper type conversions.
 */
@Service
public class ProductOrderRowMapper implements BiFunction<Row, String, ProductOrder> {

    private final ColumnConverter converter;

    public ProductOrderRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link ProductOrder} stored in the database.
     */
    @Override
    public ProductOrder apply(Row row, String prefix) {
        ProductOrder entity = new ProductOrder();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setQuantity(converter.fromRow(row, prefix + "_quantity", Integer.class));
        entity.setTotalPrice(converter.fromRow(row, prefix + "_total_price", BigDecimal.class));
        entity.setProductId(converter.fromRow(row, prefix + "_product_id", Long.class));
        entity.setCartId(converter.fromRow(row, prefix + "_cart_id", Long.class));
        return entity;
    }
}
