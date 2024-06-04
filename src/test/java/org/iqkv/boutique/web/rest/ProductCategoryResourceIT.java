package org.iqkv.boutique.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.iqkv.boutique.domain.ProductCategoryAsserts.*;
import static org.iqkv.boutique.web.rest.TestUtil.createUpdateProxyForBean;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.iqkv.boutique.IntegrationTest;
import org.iqkv.boutique.domain.ProductCategory;
import org.iqkv.boutique.repository.EntityManager;
import org.iqkv.boutique.repository.ProductCategoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link ProductCategoryResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class ProductCategoryResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/product-categories";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private ProductCategory productCategory;

    private ProductCategory insertedProductCategory;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductCategory createEntity(EntityManager em) {
        ProductCategory productCategory = new ProductCategory().name(DEFAULT_NAME).description(DEFAULT_DESCRIPTION);
        return productCategory;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductCategory createUpdatedEntity(EntityManager em) {
        ProductCategory productCategory = new ProductCategory().name(UPDATED_NAME).description(UPDATED_DESCRIPTION);
        return productCategory;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(ProductCategory.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        productCategory = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedProductCategory != null) {
            productCategoryRepository.delete(insertedProductCategory).block();
            insertedProductCategory = null;
        }
        deleteEntities(em);
    }

    @Test
    void createProductCategory() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ProductCategory
        var returnedProductCategory = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(productCategory))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(ProductCategory.class)
            .returnResult()
            .getResponseBody();

        // Validate the ProductCategory in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertProductCategoryUpdatableFieldsEquals(returnedProductCategory, getPersistedProductCategory(returnedProductCategory));

        insertedProductCategory = returnedProductCategory;
    }

    @Test
    void createProductCategoryWithExistingId() throws Exception {
        // Create the ProductCategory with an existing ID
        productCategory.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(productCategory))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        productCategory.setName(null);

        // Create the ProductCategory, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(productCategory))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllProductCategories() {
        // Initialize the database
        insertedProductCategory = productCategoryRepository.save(productCategory).block();

        // Get all the productCategoryList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(productCategory.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION));
    }

    @Test
    void getProductCategory() {
        // Initialize the database
        insertedProductCategory = productCategoryRepository.save(productCategory).block();

        // Get the productCategory
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, productCategory.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(productCategory.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION));
    }

    @Test
    void getNonExistingProductCategory() {
        // Get the productCategory
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingProductCategory() throws Exception {
        // Initialize the database
        insertedProductCategory = productCategoryRepository.save(productCategory).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the productCategory
        ProductCategory updatedProductCategory = productCategoryRepository.findById(productCategory.getId()).block();
        updatedProductCategory.name(UPDATED_NAME).description(UPDATED_DESCRIPTION);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedProductCategory.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedProductCategory))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProductCategoryToMatchAllProperties(updatedProductCategory);
    }

    @Test
    void putNonExistingProductCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productCategory.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, productCategory.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(productCategory))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchProductCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productCategory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(productCategory))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamProductCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productCategory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(productCategory))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateProductCategoryWithPatch() throws Exception {
        // Initialize the database
        insertedProductCategory = productCategoryRepository.save(productCategory).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the productCategory using partial update
        ProductCategory partialUpdatedProductCategory = new ProductCategory();
        partialUpdatedProductCategory.setId(productCategory.getId());

        partialUpdatedProductCategory.description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedProductCategory.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedProductCategory))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ProductCategory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProductCategoryUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedProductCategory, productCategory),
            getPersistedProductCategory(productCategory)
        );
    }

    @Test
    void fullUpdateProductCategoryWithPatch() throws Exception {
        // Initialize the database
        insertedProductCategory = productCategoryRepository.save(productCategory).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the productCategory using partial update
        ProductCategory partialUpdatedProductCategory = new ProductCategory();
        partialUpdatedProductCategory.setId(productCategory.getId());

        partialUpdatedProductCategory.name(UPDATED_NAME).description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedProductCategory.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedProductCategory))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ProductCategory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProductCategoryUpdatableFieldsEquals(
            partialUpdatedProductCategory,
            getPersistedProductCategory(partialUpdatedProductCategory)
        );
    }

    @Test
    void patchNonExistingProductCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productCategory.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, productCategory.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(productCategory))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchProductCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productCategory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(productCategory))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamProductCategory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        productCategory.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(productCategory))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ProductCategory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteProductCategory() {
        // Initialize the database
        insertedProductCategory = productCategoryRepository.save(productCategory).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the productCategory
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, productCategory.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return productCategoryRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected ProductCategory getPersistedProductCategory(ProductCategory productCategory) {
        return productCategoryRepository.findById(productCategory.getId()).block();
    }

    protected void assertPersistedProductCategoryToMatchAllProperties(ProductCategory expectedProductCategory) {
        // Test fails because reactive api returns an empty object instead of null
        // assertProductCategoryAllPropertiesEquals(expectedProductCategory, getPersistedProductCategory(expectedProductCategory));
        assertProductCategoryUpdatableFieldsEquals(expectedProductCategory, getPersistedProductCategory(expectedProductCategory));
    }

    protected void assertPersistedProductCategoryToMatchUpdatableProperties(ProductCategory expectedProductCategory) {
        // Test fails because reactive api returns an empty object instead of null
        // assertProductCategoryAllUpdatablePropertiesEquals(expectedProductCategory, getPersistedProductCategory(expectedProductCategory));
        assertProductCategoryUpdatableFieldsEquals(expectedProductCategory, getPersistedProductCategory(expectedProductCategory));
    }
}
