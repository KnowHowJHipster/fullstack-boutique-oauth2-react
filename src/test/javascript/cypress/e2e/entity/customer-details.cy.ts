import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityCreateCancelButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('CustomerDetails e2e test', () => {
  const customerDetailsPageUrl = '/customer-details';
  const customerDetailsPageUrlPattern = new RegExp('/customer-details(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  // const customerDetailsSample = {"gender":"MALE","phone":"943-753-4072","addressLine1":"neatly","city":"New Ruthieton","country":"Moldova"};

  let customerDetails;
  // let user;

  beforeEach(() => {
    cy.login(username, password);
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/users',
      body: {"id":"b7080c11-4b54-4ffa-a00e-a375bcf3f81f","login":"SPi","firstName":"Ellsworth","lastName":"Herman","email":"Reva86@yahoo.com","imageUrl":"as ratepayer","langKey":"spherical "},
    }).then(({ body }) => {
      user = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/customer-details+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/customer-details').as('postEntityRequest');
    cy.intercept('DELETE', '/api/customer-details/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/users', {
      statusCode: 200,
      body: [user],
    });

    cy.intercept('GET', '/api/shopping-carts', {
      statusCode: 200,
      body: [],
    });

  });
   */

  afterEach(() => {
    if (customerDetails) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/customer-details/${customerDetails.id}`,
      }).then(() => {
        customerDetails = undefined;
      });
    }
  });

  /* Disabled due to incompatibility
  afterEach(() => {
    if (user) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/users/${user.id}`,
      }).then(() => {
        user = undefined;
      });
    }
  });
   */

  it('CustomerDetails menu should load CustomerDetails page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('customer-details');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('CustomerDetails').should('exist');
    cy.url().should('match', customerDetailsPageUrlPattern);
  });

  describe('CustomerDetails page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(customerDetailsPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create CustomerDetails page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/customer-details/new$'));
        cy.getEntityCreateUpdateHeading('CustomerDetails');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', customerDetailsPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/customer-details',
          body: {
            ...customerDetailsSample,
            user: user,
          },
        }).then(({ body }) => {
          customerDetails = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/customer-details+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/customer-details?page=0&size=20>; rel="last",<http://localhost/api/customer-details?page=0&size=20>; rel="first"',
              },
              body: [customerDetails],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(customerDetailsPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(customerDetailsPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details CustomerDetails page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('customerDetails');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', customerDetailsPageUrlPattern);
      });

      it('edit button click should load edit CustomerDetails page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('CustomerDetails');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', customerDetailsPageUrlPattern);
      });

      it('edit button click should load edit CustomerDetails page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('CustomerDetails');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', customerDetailsPageUrlPattern);
      });

      it.skip('last delete button click should delete instance of CustomerDetails', () => {
        cy.intercept('GET', '/api/customer-details/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('customerDetails').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', customerDetailsPageUrlPattern);

        customerDetails = undefined;
      });
    });
  });

  describe('new CustomerDetails page', () => {
    beforeEach(() => {
      cy.visit(`${customerDetailsPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('CustomerDetails');
    });

    it.skip('should create an instance of CustomerDetails', () => {
      cy.get(`[data-cy="gender"]`).select('MALE');

      cy.get(`[data-cy="phone"]`).type('911-894-1973 x31797');
      cy.get(`[data-cy="phone"]`).should('have.value', '911-894-1973 x31797');

      cy.get(`[data-cy="addressLine1"]`).type('blissfully ferociously forenenst');
      cy.get(`[data-cy="addressLine1"]`).should('have.value', 'blissfully ferociously forenenst');

      cy.get(`[data-cy="addressLine2"]`).type('psst');
      cy.get(`[data-cy="addressLine2"]`).should('have.value', 'psst');

      cy.get(`[data-cy="city"]`).type('Mansfield');
      cy.get(`[data-cy="city"]`).should('have.value', 'Mansfield');

      cy.get(`[data-cy="country"]`).type('Ecuador');
      cy.get(`[data-cy="country"]`).should('have.value', 'Ecuador');

      cy.get(`[data-cy="user"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        customerDetails = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', customerDetailsPageUrlPattern);
    });
  });
});
