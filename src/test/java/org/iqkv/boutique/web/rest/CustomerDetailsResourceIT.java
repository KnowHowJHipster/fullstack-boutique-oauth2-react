package org.iqkv.boutique.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.iqkv.boutique.domain.CustomerDetailsAsserts.*;
import static org.iqkv.boutique.web.rest.TestUtil.createUpdateProxyForBean;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.iqkv.boutique.IntegrationTest;
import org.iqkv.boutique.domain.CustomerDetails;
import org.iqkv.boutique.domain.User;
import org.iqkv.boutique.domain.enumeration.Gender;
import org.iqkv.boutique.repository.CustomerDetailsRepository;
import org.iqkv.boutique.repository.EntityManager;
import org.iqkv.boutique.repository.UserRepository;
import org.iqkv.boutique.service.CustomerDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/**
 * Integration tests for the {@link CustomerDetailsResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class CustomerDetailsResourceIT {

    private static final Gender DEFAULT_GENDER = Gender.MALE;
    private static final Gender UPDATED_GENDER = Gender.FEMALE;

    private static final String DEFAULT_PHONE = "AAAAAAAAAA";
    private static final String UPDATED_PHONE = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS_LINE_1 = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS_LINE_1 = "BBBBBBBBBB";

    private static final String DEFAULT_ADDRESS_LINE_2 = "AAAAAAAAAA";
    private static final String UPDATED_ADDRESS_LINE_2 = "BBBBBBBBBB";

    private static final String DEFAULT_CITY = "AAAAAAAAAA";
    private static final String UPDATED_CITY = "BBBBBBBBBB";

    private static final String DEFAULT_COUNTRY = "AAAAAAAAAA";
    private static final String UPDATED_COUNTRY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/customer-details";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CustomerDetailsRepository customerDetailsRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private CustomerDetailsRepository customerDetailsRepositoryMock;

    @Mock
    private CustomerDetailsService customerDetailsServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private CustomerDetails customerDetails;

    private CustomerDetails insertedCustomerDetails;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CustomerDetails createEntity(EntityManager em) {
        CustomerDetails customerDetails = new CustomerDetails()
            .gender(DEFAULT_GENDER)
            .phone(DEFAULT_PHONE)
            .addressLine1(DEFAULT_ADDRESS_LINE_1)
            .addressLine2(DEFAULT_ADDRESS_LINE_2)
            .city(DEFAULT_CITY)
            .country(DEFAULT_COUNTRY);
        // Add required entity
        User user = em.insert(UserResourceIT.createEntity(em)).block();
        customerDetails.setUser(user);
        return customerDetails;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CustomerDetails createUpdatedEntity(EntityManager em) {
        CustomerDetails customerDetails = new CustomerDetails()
            .gender(UPDATED_GENDER)
            .phone(UPDATED_PHONE)
            .addressLine1(UPDATED_ADDRESS_LINE_1)
            .addressLine2(UPDATED_ADDRESS_LINE_2)
            .city(UPDATED_CITY)
            .country(UPDATED_COUNTRY);
        // Add required entity
        User user = em.insert(UserResourceIT.createEntity(em)).block();
        customerDetails.setUser(user);
        return customerDetails;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(CustomerDetails.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        UserResourceIT.deleteEntities(em);
    }

    @BeforeEach
    public void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    public void initTest() {
        customerDetails = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedCustomerDetails != null) {
            customerDetailsRepository.delete(insertedCustomerDetails).block();
            insertedCustomerDetails = null;
        }
        deleteEntities(em);
        userRepository.deleteAllUserAuthorities().block();
        userRepository.deleteAll().block();
    }

    @Test
    void createCustomerDetails() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the CustomerDetails
        var returnedCustomerDetails = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(CustomerDetails.class)
            .returnResult()
            .getResponseBody();

        // Validate the CustomerDetails in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertCustomerDetailsUpdatableFieldsEquals(returnedCustomerDetails, getPersistedCustomerDetails(returnedCustomerDetails));

        insertedCustomerDetails = returnedCustomerDetails;
    }

    @Test
    void createCustomerDetailsWithExistingId() throws Exception {
        // Create the CustomerDetails with an existing ID
        customerDetails.setId(1L);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CustomerDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkGenderIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        customerDetails.setGender(null);

        // Create the CustomerDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkPhoneIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        customerDetails.setPhone(null);

        // Create the CustomerDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkAddressLine1IsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        customerDetails.setAddressLine1(null);

        // Create the CustomerDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        customerDetails.setCity(null);

        // Create the CustomerDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCountryIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        customerDetails.setCountry(null);

        // Create the CustomerDetails, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllCustomerDetails() {
        // Initialize the database
        insertedCustomerDetails = customerDetailsRepository.save(customerDetails).block();

        // Get all the customerDetailsList
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
            .value(hasItem(customerDetails.getId().intValue()))
            .jsonPath("$.[*].gender")
            .value(hasItem(DEFAULT_GENDER.toString()))
            .jsonPath("$.[*].phone")
            .value(hasItem(DEFAULT_PHONE))
            .jsonPath("$.[*].addressLine1")
            .value(hasItem(DEFAULT_ADDRESS_LINE_1))
            .jsonPath("$.[*].addressLine2")
            .value(hasItem(DEFAULT_ADDRESS_LINE_2))
            .jsonPath("$.[*].city")
            .value(hasItem(DEFAULT_CITY))
            .jsonPath("$.[*].country")
            .value(hasItem(DEFAULT_COUNTRY));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCustomerDetailsWithEagerRelationshipsIsEnabled() {
        when(customerDetailsServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(customerDetailsServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCustomerDetailsWithEagerRelationshipsIsNotEnabled() {
        when(customerDetailsServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(customerDetailsRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getCustomerDetails() {
        // Initialize the database
        insertedCustomerDetails = customerDetailsRepository.save(customerDetails).block();

        // Get the customerDetails
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, customerDetails.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(customerDetails.getId().intValue()))
            .jsonPath("$.gender")
            .value(is(DEFAULT_GENDER.toString()))
            .jsonPath("$.phone")
            .value(is(DEFAULT_PHONE))
            .jsonPath("$.addressLine1")
            .value(is(DEFAULT_ADDRESS_LINE_1))
            .jsonPath("$.addressLine2")
            .value(is(DEFAULT_ADDRESS_LINE_2))
            .jsonPath("$.city")
            .value(is(DEFAULT_CITY))
            .jsonPath("$.country")
            .value(is(DEFAULT_COUNTRY));
    }

    @Test
    void getNonExistingCustomerDetails() {
        // Get the customerDetails
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingCustomerDetails() throws Exception {
        // Initialize the database
        insertedCustomerDetails = customerDetailsRepository.save(customerDetails).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the customerDetails
        CustomerDetails updatedCustomerDetails = customerDetailsRepository.findById(customerDetails.getId()).block();
        updatedCustomerDetails
            .gender(UPDATED_GENDER)
            .phone(UPDATED_PHONE)
            .addressLine1(UPDATED_ADDRESS_LINE_1)
            .addressLine2(UPDATED_ADDRESS_LINE_2)
            .city(UPDATED_CITY)
            .country(UPDATED_COUNTRY);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedCustomerDetails.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(updatedCustomerDetails))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the CustomerDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCustomerDetailsToMatchAllProperties(updatedCustomerDetails);
    }

    @Test
    void putNonExistingCustomerDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        customerDetails.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, customerDetails.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CustomerDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchCustomerDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        customerDetails.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CustomerDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamCustomerDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        customerDetails.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the CustomerDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateCustomerDetailsWithPatch() throws Exception {
        // Initialize the database
        insertedCustomerDetails = customerDetailsRepository.save(customerDetails).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the customerDetails using partial update
        CustomerDetails partialUpdatedCustomerDetails = new CustomerDetails();
        partialUpdatedCustomerDetails.setId(customerDetails.getId());

        partialUpdatedCustomerDetails.addressLine2(UPDATED_ADDRESS_LINE_2).city(UPDATED_CITY);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCustomerDetails.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedCustomerDetails))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the CustomerDetails in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCustomerDetailsUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedCustomerDetails, customerDetails),
            getPersistedCustomerDetails(customerDetails)
        );
    }

    @Test
    void fullUpdateCustomerDetailsWithPatch() throws Exception {
        // Initialize the database
        insertedCustomerDetails = customerDetailsRepository.save(customerDetails).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the customerDetails using partial update
        CustomerDetails partialUpdatedCustomerDetails = new CustomerDetails();
        partialUpdatedCustomerDetails.setId(customerDetails.getId());

        partialUpdatedCustomerDetails
            .gender(UPDATED_GENDER)
            .phone(UPDATED_PHONE)
            .addressLine1(UPDATED_ADDRESS_LINE_1)
            .addressLine2(UPDATED_ADDRESS_LINE_2)
            .city(UPDATED_CITY)
            .country(UPDATED_COUNTRY);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedCustomerDetails.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedCustomerDetails))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the CustomerDetails in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCustomerDetailsUpdatableFieldsEquals(
            partialUpdatedCustomerDetails,
            getPersistedCustomerDetails(partialUpdatedCustomerDetails)
        );
    }

    @Test
    void patchNonExistingCustomerDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        customerDetails.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, customerDetails.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CustomerDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchCustomerDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        customerDetails.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the CustomerDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamCustomerDetails() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        customerDetails.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(customerDetails))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the CustomerDetails in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteCustomerDetails() {
        // Initialize the database
        insertedCustomerDetails = customerDetailsRepository.save(customerDetails).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the customerDetails
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, customerDetails.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return customerDetailsRepository.count().block();
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

    protected CustomerDetails getPersistedCustomerDetails(CustomerDetails customerDetails) {
        return customerDetailsRepository.findById(customerDetails.getId()).block();
    }

    protected void assertPersistedCustomerDetailsToMatchAllProperties(CustomerDetails expectedCustomerDetails) {
        // Test fails because reactive api returns an empty object instead of null
        // assertCustomerDetailsAllPropertiesEquals(expectedCustomerDetails, getPersistedCustomerDetails(expectedCustomerDetails));
        assertCustomerDetailsUpdatableFieldsEquals(expectedCustomerDetails, getPersistedCustomerDetails(expectedCustomerDetails));
    }

    protected void assertPersistedCustomerDetailsToMatchUpdatableProperties(CustomerDetails expectedCustomerDetails) {
        // Test fails because reactive api returns an empty object instead of null
        // assertCustomerDetailsAllUpdatablePropertiesEquals(expectedCustomerDetails, getPersistedCustomerDetails(expectedCustomerDetails));
        assertCustomerDetailsUpdatableFieldsEquals(expectedCustomerDetails, getPersistedCustomerDetails(expectedCustomerDetails));
    }
}
