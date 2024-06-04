package org.iqkv.boutique.repository;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.List;
import org.iqkv.boutique.domain.ProductOrder;
import org.iqkv.boutique.repository.rowmapper.ProductOrderRowMapper;
import org.iqkv.boutique.repository.rowmapper.ProductRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the ProductOrder entity.
 */
@SuppressWarnings("unused")
class ProductOrderRepositoryInternalImpl extends SimpleR2dbcRepository<ProductOrder, Long> implements ProductOrderRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final ProductRowMapper productMapper;
    private final ShoppingCartRowMapper shoppingcartMapper;
    private final ProductOrderRowMapper productorderMapper;

    private static final Table entityTable = Table.aliased("product_order", EntityManager.ENTITY_ALIAS);
    private static final Table productTable = Table.aliased("product", "product");
    private static final Table cartTable = Table.aliased("shopping_cart", "cart");

    public ProductOrderRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        ProductRowMapper productMapper,
        ShoppingCartRowMapper shoppingcartMapper,
        ProductOrderRowMapper productorderMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(ProductOrder.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.productMapper = productMapper;
        this.shoppingcartMapper = shoppingcartMapper;
        this.productorderMapper = productorderMapper;
    }

    @Override
    public Flux<ProductOrder> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<ProductOrder> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = ProductOrderSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(ProductSqlHelper.getColumns(productTable, "product"));
        columns.addAll(ShoppingCartSqlHelper.getColumns(cartTable, "cart"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(productTable)
            .on(Column.create("product_id", entityTable))
            .equals(Column.create("id", productTable))
            .leftOuterJoin(cartTable)
            .on(Column.create("cart_id", entityTable))
            .equals(Column.create("id", cartTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, ProductOrder.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<ProductOrder> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<ProductOrder> findById(Long id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(id.toString()));
        return createQuery(null, whereClause).one();
    }

    @Override
    public Mono<ProductOrder> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<ProductOrder> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<ProductOrder> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private ProductOrder process(Row row, RowMetadata metadata) {
        ProductOrder entity = productorderMapper.apply(row, "e");
        entity.setProduct(productMapper.apply(row, "product"));
        entity.setCart(shoppingcartMapper.apply(row, "cart"));
        return entity;
    }

    @Override
    public <S extends ProductOrder> Mono<S> save(S entity) {
        return super.save(entity);
    }
}
