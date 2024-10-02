import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Login from '../Login';
import { ENV } from '../../../utils/env';
import './../../../locale';
import { MemoryRouter } from 'react-router-dom';
import { productId2ProductTitle } from '../../../utils/src/lib/utils/productId2ProductTitle';
import { vi } from 'vitest';

const oldWindowLocation = global.window.location;

beforeAll(() => {
  // eslint-disable-next-line functional/immutable-data
  Object.defineProperty(window, 'location', { value: { assign: vi.fn() } });
});
afterAll(() => {
  // eslint-disable-next-line functional/immutable-data
  Object.defineProperty(window, 'location', { value: oldWindowLocation });
});

vi.spyOn(URLSearchParams.prototype, 'get');

global.window.open = vi.fn();

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

    expect(URLSearchParams.prototype.get).toHaveBeenCalledTimes(expectedCalledTimes);
  });
});

test('Test: Trying to access the login with SPID', () => {
  render(<Login />);
  const buttonSpid = document.getElementById('spidButton');
  fireEvent.click(buttonSpid as HTMLElement);
});

test('Test: Trying to access the login with CIE', () => {
  render(<Login />);
  const buttonCIE = screen.getByRole('button', {
    name: 'Entra con CIE',
  });
  fireEvent.click(buttonCIE);
  expect(global.window.location.assign).toHaveBeenCalledWith(
    `${ENV.URL_API.AUTHORIZE}?idp=${ENV.SPID_CIE_ENTITY_ID}`
  );
});

test('Test: Click in the conditions and privacy links below the login methods', () => {
  render(<Login />);

  const termsConditionLink = screen.getByText('Termini e condizioni d’uso');
  const privacyLink = screen.getAllByText(/Informativa Privacy/)[0];

  fireEvent.click(termsConditionLink);
  expect(global.window.location.assign).toHaveBeenCalledWith(ENV.URL_FOOTER.TERMS_AND_CONDITIONS);

  fireEvent.click(privacyLink);
  expect(global.window.location.assign).toHaveBeenCalledWith(ENV.URL_FOOTER.PRIVACY_DISCLAIMER);
});
