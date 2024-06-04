package org.iqkv.boutique.service;

import org.iqkv.boutique.domain.CustomerDetails;
import org.iqkv.boutique.repository.CustomerDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link org.iqkv.boutique.domain.CustomerDetails}.
 */
@Service
@Transactional
public class CustomerDetailsService {

    private final Logger log = LoggerFactory.getLogger(CustomerDetailsService.class);

    private final CustomerDetailsRepository customerDetailsRepository;

    public CustomerDetailsService(CustomerDetailsRepository customerDetailsRepository) {
        this.customerDetailsRepository = customerDetailsRepository;
    }

    /**
     * Save a customerDetails.
     *
     * @param customerDetails the entity to save.
     * @return the persisted entity.
     */
    public Mono<CustomerDetails> save(CustomerDetails customerDetails) {
        log.debug("Request to save CustomerDetails : {}", customerDetails);
        return customerDetailsRepository.save(customerDetails);
    }

    /**
     * Update a customerDetails.
     *
     * @param customerDetails the entity to save.
     * @return the persisted entity.
     */
    public Mono<CustomerDetails> update(CustomerDetails customerDetails) {
        log.debug("Request to update CustomerDetails : {}", customerDetails);
        return customerDetailsRepository.save(customerDetails);
    }

    /**
     * Partially update a customerDetails.
     *
     * @param customerDetails the entity to update partially.
     * @return the persisted entity.
     */
    public Mono<CustomerDetails> partialUpdate(CustomerDetails customerDetails) {
        log.debug("Request to partially update CustomerDetails : {}", customerDetails);

        return customerDetailsRepository
            .findById(customerDetails.getId())
            .map(existingCustomerDetails -> {
                if (customerDetails.getGender() != null) {
                    existingCustomerDetails.setGender(customerDetails.getGender());
                }
                if (customerDetails.getPhone() != null) {
                    existingCustomerDetails.setPhone(customerDetails.getPhone());
                }
                if (customerDetails.getAddressLine1() != null) {
                    existingCustomerDetails.setAddressLine1(customerDetails.getAddressLine1());
                }
                if (customerDetails.getAddressLine2() != null) {
                    existingCustomerDetails.setAddressLine2(customerDetails.getAddressLine2());
                }
                if (customerDetails.getCity() != null) {
                    existingCustomerDetails.setCity(customerDetails.getCity());
                }
                if (customerDetails.getCountry() != null) {
                    existingCustomerDetails.setCountry(customerDetails.getCountry());
                }

                return existingCustomerDetails;
            })
            .flatMap(customerDetailsRepository::save);
    }

    /**
     * Get all the customerDetails.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Flux<CustomerDetails> findAll(Pageable pageable) {
        log.debug("Request to get all CustomerDetails");
        return customerDetailsRepository.findAllBy(pageable);
    }

    /**
     * Get all the customerDetails with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Flux<CustomerDetails> findAllWithEagerRelationships(Pageable pageable) {
        return customerDetailsRepository.findAllWithEagerRelationships(pageable);
    }

    /**
     * Returns the number of customerDetails available.
     * @return the number of entities in the database.
     *
     */
    public Mono<Long> countAll() {
        return customerDetailsRepository.count();
    }

    /**
     * Get one customerDetails by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Mono<CustomerDetails> findOne(Long id) {
        log.debug("Request to get CustomerDetails : {}", id);
        return customerDetailsRepository.findOneWithEagerRelationships(id);
    }

    /**
     * Delete the customerDetails by id.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    public Mono<Void> delete(Long id) {
        log.debug("Request to delete CustomerDetails : {}", id);
        return customerDetailsRepository.deleteById(id);
    }
}
