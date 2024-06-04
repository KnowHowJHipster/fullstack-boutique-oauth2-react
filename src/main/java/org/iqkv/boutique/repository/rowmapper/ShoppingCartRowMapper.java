package org.iqkv.boutique.repository.rowmapper;

import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.BiFunction;
import org.iqkv.boutique.domain.ShoppingCart;
import org.iqkv.boutique.domain.enumeration.OrderStatus;
import org.iqkv.boutique.domain.enumeration.PaymentMethod;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link ShoppingCart}, with proper type conversions.
 */
@Service
public class ShoppingCartRowMapper implements BiFunction<Row, String, ShoppingCart> {

    private final ColumnConverter converter;

    public ShoppingCartRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link ShoppingCart} stored in the database.
     */
    @Override
    public ShoppingCart apply(Row row, String prefix) {
        ShoppingCart entity = new ShoppingCart();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setPlacedDate(converter.fromRow(row, prefix + "_placed_date", Instant.class));
        entity.setStatus(converter.fromRow(row, prefix + "_status", OrderStatus.class));
        entity.setTotalPrice(converter.fromRow(row, prefix + "_total_price", BigDecimal.class));
        entity.setPaymentMethod(converter.fromRow(row, prefix + "_payment_method", PaymentMethod.class));
        entity.setPaymentReference(converter.fromRow(row, prefix + "_payment_reference", String.class));
        entity.setCustomerDetailsId(converter.fromRow(row, prefix + "_customer_details_id", Long.class));
        return entity;
    }
}
