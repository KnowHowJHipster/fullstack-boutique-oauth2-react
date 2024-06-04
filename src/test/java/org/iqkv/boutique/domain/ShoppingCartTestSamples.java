package org.iqkv.boutique.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ShoppingCartTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static ShoppingCart getShoppingCartSample1() {
        return new ShoppingCart().id(1L).paymentReference("paymentReference1");
    }

    public static ShoppingCart getShoppingCartSample2() {
        return new ShoppingCart().id(2L).paymentReference("paymentReference2");
    }

    public static ShoppingCart getShoppingCartRandomSampleGenerator() {
        return new ShoppingCart().id(longCount.incrementAndGet()).paymentReference(UUID.randomUUID().toString());
    }
}
