package org.iqkv.boutique.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.iqkv.boutique.domain.ProductOrderTestSamples.*;
import static org.iqkv.boutique.domain.ProductTestSamples.*;
import static org.iqkv.boutique.domain.ShoppingCartTestSamples.*;

import org.iqkv.boutique.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProductOrderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProductOrder.class);
        ProductOrder productOrder1 = getProductOrderSample1();
        ProductOrder productOrder2 = new ProductOrder();
        assertThat(productOrder1).isNotEqualTo(productOrder2);

        productOrder2.setId(productOrder1.getId());
        assertThat(productOrder1).isEqualTo(productOrder2);

        productOrder2 = getProductOrderSample2();
        assertThat(productOrder1).isNotEqualTo(productOrder2);
    }

    @Test
    void productTest() {
        ProductOrder productOrder = getProductOrderRandomSampleGenerator();
        Product productBack = getProductRandomSampleGenerator();

        productOrder.setProduct(productBack);
        assertThat(productOrder.getProduct()).isEqualTo(productBack);

        productOrder.product(null);
        assertThat(productOrder.getProduct()).isNull();
    }

    @Test
    void cartTest() {
        ProductOrder productOrder = getProductOrderRandomSampleGenerator();
        ShoppingCart shoppingCartBack = getShoppingCartRandomSampleGenerator();

        productOrder.setCart(shoppingCartBack);
        assertThat(productOrder.getCart()).isEqualTo(shoppingCartBack);

        productOrder.cart(null);
        assertThat(productOrder.getCart()).isNull();
    }
}
