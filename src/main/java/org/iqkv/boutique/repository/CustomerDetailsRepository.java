package org.iqkv.boutique.repository;

import org.iqkv.boutique.domain.CustomerDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the CustomerDetails entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CustomerDetailsRepository extends ReactiveCrudRepository<CustomerDetails, Long>, CustomerDetailsRepositoryInternal {
    Flux<CustomerDetails> findAllBy(Pageable pageable);

    @Override
    Mono<CustomerDetails> findOneWithEagerRelationships(Long id);

    @Override
    Flux<CustomerDetails> findAllWithEagerRelationships();

    @Override
    Flux<CustomerDetails> findAllWithEagerRelationships(Pageable page);

    @Query("SELECT * FROM customer_details entity WHERE entity.user_id = :id")
    Flux<CustomerDetails> findByUser(Long id);

    @Query("SELECT * FROM customer_details entity WHERE entity.user_id IS NULL")
    Flux<CustomerDetails> findAllWhereUserIsNull();

    @Override
    <S extends CustomerDetails> Mono<S> save(S entity);

    @Override
    Flux<CustomerDetails> findAll();

    @Override
    Mono<CustomerDetails> findById(Long id);

    @Override
    Mono<Void> deleteById(Long id);
}

interface CustomerDetailsRepositoryInternal {
    <S extends CustomerDetails> Mono<S> save(S entity);

    Flux<CustomerDetails> findAllBy(Pageable pageable);

    Flux<CustomerDetails> findAll();

    Mono<CustomerDetails> findById(Long id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<CustomerDetails> findAllBy(Pageable pageable, Criteria criteria);

    Mono<CustomerDetails> findOneWithEagerRelationships(Long id);

    Flux<CustomerDetails> findAllWithEagerRelationships();

    Flux<CustomerDetails> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
