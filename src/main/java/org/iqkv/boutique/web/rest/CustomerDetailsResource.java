package org.iqkv.boutique.web.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import org.iqkv.boutique.domain.CustomerDetails;
import org.iqkv.boutique.repository.CustomerDetailsRepository;
import org.iqkv.boutique.service.CustomerDetailsService;
import org.iqkv.boutique.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link org.iqkv.boutique.domain.CustomerDetails}.
 */
@RestController
@RequestMapping("/api/customer-details")
public class CustomerDetailsResource {

    private final Logger log = LoggerFactory.getLogger(CustomerDetailsResource.class);

    private static final String ENTITY_NAME = "customerDetails";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CustomerDetailsService customerDetailsService;

    private final CustomerDetailsRepository customerDetailsRepository;

    public CustomerDetailsResource(CustomerDetailsService customerDetailsService, CustomerDetailsRepository customerDetailsRepository) {
        this.customerDetailsService = customerDetailsService;
        this.customerDetailsRepository = customerDetailsRepository;
    }

    /**
     * {@code POST  /customer-details} : Create a new customerDetails.
     *
     * @param customerDetails the customerDetails to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new customerDetails, or with status {@code 400 (Bad Request)} if the customerDetails has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<CustomerDetails>> createCustomerDetails(@Valid @RequestBody CustomerDetails customerDetails)
        throws URISyntaxException {
        log.debug("REST request to save CustomerDetails : {}", customerDetails);
        if (customerDetails.getId() != null) {
            throw new BadRequestAlertException("A new customerDetails cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return customerDetailsService
            .save(customerDetails)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/customer-details/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /customer-details/:id} : Updates an existing customerDetails.
     *
     * @param id the id of the customerDetails to save.
     * @param customerDetails the customerDetails to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated customerDetails,
     * or with status {@code 400 (Bad Request)} if the customerDetails is not valid,
     * or with status {@code 500 (Internal Server Error)} if the customerDetails couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<CustomerDetails>> updateCustomerDetails(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CustomerDetails customerDetails
    ) throws URISyntaxException {
        log.debug("REST request to update CustomerDetails : {}, {}", id, customerDetails);
        if (customerDetails.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, customerDetails.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return customerDetailsRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return customerDetailsService
                    .update(customerDetails)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(
                        result ->
                            ResponseEntity.ok()
                                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                                .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /customer-details/:id} : Partial updates given fields of an existing customerDetails, field will ignore if it is null
     *
     * @param id the id of the customerDetails to save.
     * @param customerDetails the customerDetails to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated customerDetails,
     * or with status {@code 400 (Bad Request)} if the customerDetails is not valid,
     * or with status {@code 404 (Not Found)} if the customerDetails is not found,
     * or with status {@code 500 (Internal Server Error)} if the customerDetails couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<CustomerDetails>> partialUpdateCustomerDetails(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CustomerDetails customerDetails
    ) throws URISyntaxException {
        log.debug("REST request to partial update CustomerDetails partially : {}, {}", id, customerDetails);
        if (customerDetails.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, customerDetails.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return customerDetailsRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<CustomerDetails> result = customerDetailsService.partialUpdate(customerDetails);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(
                        res ->
                            ResponseEntity.ok()
                                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                                .body(res)
                    );
            });
    }

    /**
     * {@code GET  /customer-details} : get all the customerDetails.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of customerDetails in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<CustomerDetails>>> getAllCustomerDetails(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request,
        @RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload
    ) {
        log.debug("REST request to get a page of CustomerDetails");
        return customerDetailsService
            .countAll()
            .zipWith(customerDetailsService.findAll(pageable).collectList())
            .map(
                countWithEntities ->
                    ResponseEntity.ok()
                        .headers(
                            PaginationUtil.generatePaginationHttpHeaders(
                                ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                                new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                            )
                        )
                        .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /customer-details/:id} : get the "id" customerDetails.
     *
     * @param id the id of the customerDetails to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the customerDetails, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CustomerDetails>> getCustomerDetails(@PathVariable("id") Long id) {
        log.debug("REST request to get CustomerDetails : {}", id);
        Mono<CustomerDetails> customerDetails = customerDetailsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(customerDetails);
    }

    /**
     * {@code DELETE  /customer-details/:id} : delete the "id" customerDetails.
     *
     * @param id the id of the customerDetails to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCustomerDetails(@PathVariable("id") Long id) {
        log.debug("REST request to delete CustomerDetails : {}", id);
        return customerDetailsService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
