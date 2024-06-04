package org.iqkv.boutique.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.iqkv.boutique.domain.CustomerDetailsTestSamples.*;
import static org.iqkv.boutique.domain.ProductOrderTestSamples.*;
import static org.iqkv.boutique.domain.ShoppingCartTestSamples.*;

import java.util.HashSet;
import java.util.Set;
import org.iqkv.boutique.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ShoppingCartTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ShoppingCart.class);
        ShoppingCart shoppingCart1 = getShoppingCartSample1();
        ShoppingCart shoppingCart2 = new ShoppingCart();
        assertThat(shoppingCart1).isNotEqualTo(shoppingCart2);

        shoppingCart2.setId(shoppingCart1.getId());
        assertThat(shoppingCart1).isEqualTo(shoppingCart2);

        shoppingCart2 = getShoppingCartSample2();
        assertThat(shoppingCart1).isNotEqualTo(shoppingCart2);
    }

    @Test
    void orderTest() {
        ShoppingCart shoppingCart = getShoppingCartRandomSampleGenerator();
        ProductOrder productOrderBack = getProductOrderRandomSampleGenerator();

        shoppingCart.addOrder(productOrderBack);
        assertThat(shoppingCart.getOrders()).containsOnly(productOrderBack);
        assertThat(productOrderBack.getCart()).isEqualTo(shoppingCart);

        shoppingCart.removeOrder(productOrderBack);
        assertThat(shoppingCart.getOrders()).doesNotContain(productOrderBack);
        assertThat(productOrderBack.getCart()).isNull();

        shoppingCart.orders(new HashSet<>(Set.of(productOrderBack)));
        assertThat(shoppingCart.getOrders()).containsOnly(productOrderBack);
        assertThat(productOrderBack.getCart()).isEqualTo(shoppingCart);

        shoppingCart.setOrders(new HashSet<>());
        assertThat(shoppingCart.getOrders()).doesNotContain(productOrderBack);
        assertThat(productOrderBack.getCart()).isNull();
    }

    @Test
    void customerDetailsTest() {
        ShoppingCart shoppingCart = getShoppingCartRandomSampleGenerator();
        CustomerDetails customerDetailsBack = getCustomerDetailsRandomSampleGenerator();

        shoppingCart.setCustomerDetails(customerDetailsBack);
        assertThat(shoppingCart.getCustomerDetails()).isEqualTo(customerDetailsBack);

        shoppingCart.customerDetails(null);
        assertThat(shoppingCart.getCustomerDetails()).isNull();
    }
}
