package org.iqkv.boutique.service;

import org.iqkv.boutique.domain.ShoppingCart;
import org.iqkv.boutique.repository.ShoppingCartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link org.iqkv.boutique.domain.ShoppingCart}.
 */
@Service
@Transactional
public class ShoppingCartService {

    private final Logger log = LoggerFactory.getLogger(ShoppingCartService.class);

    private final ShoppingCartRepository shoppingCartRepository;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository) {
        this.shoppingCartRepository = shoppingCartRepository;
    }

    /**
     * Save a shoppingCart.
     *
     * @param shoppingCart the entity to save.
     * @return the persisted entity.
     */
    public Mono<ShoppingCart> save(ShoppingCart shoppingCart) {
        log.debug("Request to save ShoppingCart : {}", shoppingCart);
        return shoppingCartRepository.save(shoppingCart);
    }

    /**
     * Update a shoppingCart.
     *
     * @param shoppingCart the entity to save.
     * @return the persisted entity.
     */
    public Mono<ShoppingCart> update(ShoppingCart shoppingCart) {
        log.debug("Request to update ShoppingCart : {}", shoppingCart);
        return shoppingCartRepository.save(shoppingCart);
    }

    /**
     * Partially update a shoppingCart.
     *
     * @param shoppingCart the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<ShoppingCart> partialUpdate(ShoppingCart shoppingCart) {
        log.debug("Request to partially update ShoppingCart : {}", shoppingCart);

        return shoppingCartRepository
            .findById(shoppingCart.getId())
            .map(existingShoppingCart -> {
                if (shoppingCart.getPlacedDate() != null) {
                    existingShoppingCart.setPlacedDate(shoppingCart.getPlacedDate());
                }
                if (shoppingCart.getStatus() != null) {
                    existingShoppingCart.setStatus(shoppingCart.getStatus());
                }
                if (shoppingCart.getTotalPrice() != null) {
                    existingShoppingCart.setTotalPrice(shoppingCart.getTotalPrice());
                }
                if (shoppingCart.getPaymentMethod() != null) {
                    existingShoppingCart.setPaymentMethod(shoppingCart.getPaymentMethod());
                }
                if (shoppingCart.getPaymentReference() != null) {
                    existingShoppingCart.setPaymentReference(shoppingCart.getPaymentReference());
                }

                return existingShoppingCart;
            })
            .flatMap(shoppingCartRepository::save);
    }

    /**
     * Get all the shoppingCarts.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<ShoppingCart> findAll() {
        log.debug("Request to get all ShoppingCarts");
        return shoppingCartRepository.findAll();
    }

    /**
     * Returns the number of shoppingCarts available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return shoppingCartRepository.count();
    }

    /**
     * Get one shoppingCart by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<ShoppingCart> findOne(Long id) {
        log.debug("Request to get ShoppingCart : {}", id);
        return shoppingCartRepository.findById(id);
    }

    /**
     * Delete the shoppingCart by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete ShoppingCart : {}", id);
        return shoppingCartRepository.deleteById(id);
    }
}
