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

describe('ShoppingCart e2e test', () => {
  const shoppingCartPageUrl = '/shopping-cart';
  const shoppingCartPageUrlPattern = new RegExp('/shopping-cart(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  // const shoppingCartSample = {"placedDate":"2024-06-04T15:43:12.595Z","status":"PAID","totalPrice":6775.74,"paymentMethod":"CREDIT_CARD"};

  let shoppingCart;
  // let customerDetails;

  beforeEach(() => {
    cy.login(username, password);
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/customer-details',
      body: {"gender":"FEMALE","phone":"316.335.7837 x2395","addressLine1":"since delightfully worried","addressLine2":"ATM personal","city":"Jerrodfurt","country":"Guadeloupe"},
    }).then(({ body }) => {
      customerDetails = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/shopping-carts+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/shopping-carts').as('postEntityRequest');
    cy.intercept('DELETE', '/api/shopping-carts/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/product-orders', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/customer-details', {
      statusCode: 200,
      body: [customerDetails],
    });

  });
   */

  afterEach(() => {
    if (shoppingCart) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/shopping-carts/${shoppingCart.id}`,
      }).then(() => {
        shoppingCart = undefined;
      });
    }
  });

  /* Disabled due to incompatibility
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
   */

  it('ShoppingCarts menu should load ShoppingCarts page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('shopping-cart');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('ShoppingCart').should('exist');
    cy.url().should('match', shoppingCartPageUrlPattern);
  });

  describe('ShoppingCart page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(shoppingCartPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create ShoppingCart page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/shopping-cart/new$'));
        cy.getEntityCreateUpdateHeading('ShoppingCart');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', shoppingCartPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/shopping-carts',
          body: {
            ...shoppingCartSample,
            customerDetails: customerDetails,
          },
        }).then(({ body }) => {
          shoppingCart = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/shopping-carts+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [shoppingCart],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(shoppingCartPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(shoppingCartPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details ShoppingCart page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('shoppingCart');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', shoppingCartPageUrlPattern);
      });

      it('edit button click should load edit ShoppingCart page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('ShoppingCart');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', shoppingCartPageUrlPattern);
      });

      it('edit button click should load edit ShoppingCart page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('ShoppingCart');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', shoppingCartPageUrlPattern);
      });

      it.skip('last delete button click should delete instance of ShoppingCart', () => {
        cy.intercept('GET', '/api/shopping-carts/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('shoppingCart').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', shoppingCartPageUrlPattern);

        shoppingCart = undefined;
      });
    });
  });

  describe('new ShoppingCart page', () => {
    beforeEach(() => {
      cy.visit(`${shoppingCartPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('ShoppingCart');
    });

    it.skip('should create an instance of ShoppingCart', () => {
      cy.get(`[data-cy="placedDate"]`).type('2024-06-04T02:47');
      cy.get(`[data-cy="placedDate"]`).blur();
      cy.get(`[data-cy="placedDate"]`).should('have.value', '2024-06-04T02:47');

      cy.get(`[data-cy="status"]`).select('COMPLETED');

      cy.get(`[data-cy="totalPrice"]`).type('24685.01');
      cy.get(`[data-cy="totalPrice"]`).should('have.value', '24685.01');

      cy.get(`[data-cy="paymentMethod"]`).select('CREDIT_CARD');

      cy.get(`[data-cy="paymentReference"]`).type('which splendid');
      cy.get(`[data-cy="paymentReference"]`).should('have.value', 'which splendid');

      cy.get(`[data-cy="customerDetails"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        shoppingCart = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', shoppingCartPageUrlPattern);
    });
  });
});
