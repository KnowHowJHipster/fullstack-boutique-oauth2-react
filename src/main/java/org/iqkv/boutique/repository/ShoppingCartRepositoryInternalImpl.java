package org.iqkv.boutique.repository;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import org.iqkv.boutique.domain.ShoppingCart;
import org.iqkv.boutique.repository.rowmapper.CustomerDetailsRowMapper;
import org.iqkv.boutique.repository.rowmapper.ShoppingCartRowMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC custom repository implementation for the ShoppingCart entity.
 */
@SuppressWarnings("unused")
class ShoppingCartRepositoryInternalImpl extends SimpleR2dbcRepository<ShoppingCart, Long> implements ShoppingCartRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final CustomerDetailsRowMapper customerdetailsMapper;
    private final ShoppingCartRowMapper shoppingcartMapper;

    private static final Table entityTable = Table.aliased("shopping_cart", EntityManager.ENTITY_ALIAS);
    private static final Table customerDetailsTable = Table.aliased("customer_details", "customerDetails");

    public ShoppingCartRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        CustomerDetailsRowMapper customerdetailsMapper,
        ShoppingCartRowMapper shoppingcartMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(ShoppingCart.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.customerdetailsMapper = customerdetailsMapper;
        this.shoppingcartMapper = shoppingcartMapper;
    }

    @Override
    public Flux<ShoppingCart> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<ShoppingCart> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = ShoppingCartSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(CustomerDetailsSqlHelper.getColumns(customerDetailsTable, "customerDetails"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(customerDetailsTable)
            .on(Column.create("customer_details_id", entityTable))
            .equals(Column.create("id", customerDetailsTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, ShoppingCart.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<ShoppingCart> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<ShoppingCart> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    private ShoppingCart process(Row row, RowMetadata metadata) {
        ShoppingCart entity = shoppingcartMapper.apply(row, "e");
        entity.setCustomerDetails(customerdetailsMapper.apply(row, "customerDetails"));
        return entity;
    }

    @Override
    public <S extends ShoppingCart> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
