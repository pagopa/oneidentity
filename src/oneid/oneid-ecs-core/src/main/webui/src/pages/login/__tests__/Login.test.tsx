import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Login from '../Login';
import { ENV } from '../../../utils/env';
import './../../../locale';
import { productId2ProductTitle } from '@pagopa/selfcare-common-frontend/utils/productId2ProductTitle';
import { MemoryRouter } from 'react-router-dom';

const oldWindowLocation = global.window.location;

beforeAll(() => {
  // eslint-disable-next-line functional/immutable-data
  Object.defineProperty(window, 'location', { value: { assign: jest.fn() } });
});
afterAll(() => {
  // eslint-disable-next-line functional/immutable-data
  Object.defineProperty(window, 'location', { value: oldWindowLocation });
});

jest.spyOn(URLSearchParams.prototype, 'get');

global.window.open = jest.fn();

test('Test: Session not found while trying to access dashboard: "Selfcare" Login is displayed', async () => {
  render(
    <MemoryRouter initialEntries={[{ pathname: '/', search: '?onSuccess=dashboard' }]}>
      <Login />
    </MemoryRouter>
  );
  await waitFor(() => screen.getByText('Accedi all’Area Riservata'));
  expect(URLSearchParams.prototype.get).toBeCalledTimes(3);
});

test('Test: Session not found while trying to access at onboarding flow product: "Onboarding" Login is displayed', async () => {
  const productIds = [
    'prod-interop',
    'prod-io',
    'prod-io-premium',
    'prod-io-sign',
    'prod-pn',
    'prod-pagopa',
    'prod-cgn',
    'prod-ciban',
  ];

  productIds.map(async (pid) => {
    const productTitle = productId2ProductTitle(pid);
    const expectedCalledTimes = pid === 'prod-io-premium' ? 2 : 1;
    const search =
      pid === 'prod-io-premium'
        ? `?onSuccess=onboarding/prod-io/${pid}`
        : `?onSuccess=onboarding/${pid}`;
    await waitFor(() =>
      render(
        <MemoryRouter initialEntries={[{ pathname: '/', search }]}>
          <Login />
        </MemoryRouter>
      )
    );
    await waitFor(() => {
      screen.getByText('Come vuoi accedere?');
      expect(productTitle).toBeDefined();
    });

    expect(URLSearchParams.prototype.get).toBeCalledTimes(expectedCalledTimes);
  });
});

test('Test: Session not found while trying to access at upload contract flow: "Selfcare" Login is displayed', async () => {
  const mockedJwt = 'mockJwt';
  render(
    <MemoryRouter
      initialEntries={[{ pathname: '/', search: `?onSuccess=onboarding/confirm?jwt=${mockedJwt}` }]}
    >
      <Login />
    </MemoryRouter>
  );
  await waitFor(() => screen.getByText('Accedi all’Area Riservata'));

  expect(URLSearchParams.prototype.get).toBeCalledTimes(3);
});

test('Test: Trying to access the login with SPID', () => {
  render(<Login />);
  const buttonSpid = document.getElementById('spidButton');
  fireEvent.click(buttonSpid);
});

test('Test: Trying to access the login with CIE', () => {
  render(<Login />);
  const buttonCIE = screen.getByRole('button', {
    name: 'Entra con CIE',
  });
  fireEvent.click(buttonCIE);
  expect(global.window.location.assign).toBeCalledWith(
    `${ENV.URL_API.LOGIN}/login?entityID=xx_servizicie_test&authLevel=SpidL2`
  );
});

test('Test: Access to operative manual', () => {
  render(<Login />);
  const documentationButton = screen.getByRole('button', {
    name: 'Manuale operativo',
  });

  fireEvent.click(documentationButton);
  expect(global.window.open).toBeCalledWith(ENV.URL_DOCUMENTATION, '_blank');
});

test('Test: Click in the conditions and privacy links below the login methods', () => {
  render(<Login />);

  const termsConditionLink = screen.getByText('Termini e condizioni d’uso');
  const privacyLink = screen.getAllByText(/Informativa Privacy/)[0];

  fireEvent.click(termsConditionLink);
  expect(global.window.location.assign).toBeCalledWith(ENV.URL_FOOTER.TERMS_AND_CONDITIONS);

  fireEvent.click(privacyLink);
  expect(global.window.location.assign).toBeCalledWith(ENV.URL_FOOTER.PRIVACY_DISCLAIMER);
});
