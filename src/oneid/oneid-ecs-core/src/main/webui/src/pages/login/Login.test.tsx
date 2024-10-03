/* eslint-disable functional/immutable-data */
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { afterAll, beforeAll, expect, Mock, test, vi } from 'vitest';
import { ENV } from '../../utils/env';
import { productId2ProductTitle } from '../../utils/src/lib/utils/productId2ProductTitle';
import Login from './Login';
import '../../locale';

// Mock fetch
global.fetch = vi.fn();

const oldWindowLocation = global.window.location;

beforeAll(() => {
  // Mock window location
  Object.defineProperty(window, 'location', { value: { assign: vi.fn() } });
});

afterAll(() => {
  Object.defineProperty(window, 'location', { value: oldWindowLocation });
});

// Clear mocks after each test
afterEach(() => {
  vi.clearAllMocks();
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

  void productIds.map(async (pid) => {
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
test('Renders Login component', () => {
  render(<Login />);
  expect(screen.getByText('Accedi all’Area Riservata')).toBeInTheDocument();
});

test('Fetches and displays banner alerts', async () => {
  // Mock the fetch response
  const mockBannerResponse = [
    { enable: true, severity: 'warning', description: 'This is a warning!' },
  ];
  (fetch as Mock).mockResolvedValueOnce({
    json: vi.fn().mockResolvedValueOnce(mockBannerResponse),
  });

  render(<Login />);

  await waitFor(() => {
    expect(screen.getByText('This is a warning!')).toBeInTheDocument();
  });
});

test('Handles fetch error for alert message', async () => {
  (fetch as Mock).mockRejectedValueOnce(new Error('Fetch failed'));

  render(<Login />);

  // Optionally check if an error message or warning is displayed
  await waitFor(() => {
    expect(screen.queryByText('This is a warning!')).not.toBeInTheDocument();
  });
});

test('Fetches IDP list on mount', async () => {
  const mockIDPListResponse = {
    identityProviders: [{ entityID: 'test-idp' }],
    richiediSpid: 'https://example.com/spid',
  };
  (fetch as Mock).mockResolvedValueOnce({
    json: vi.fn().mockResolvedValueOnce(mockIDPListResponse),
  });

  render(<Login />);

  await waitFor(() => {
    expect(fetch).toHaveBeenCalledWith(ENV.JSON_URL.IDP_LIST);
  });
});

test('Handles invalid client ID gracefully', async () => {
  window.history.pushState({}, '', `?client_id=invalidId`);

  render(<Login />);

  await waitFor(() => {
    expect(screen.queryByAltText('Test Client')).not.toBeInTheDocument();
  });
});

test('Clicking SPID button opens modal', () => {
  render(<Login />);
  const buttonSpid = document.getElementById('spidButton');
  fireEvent.click(buttonSpid as HTMLElement);

  expect(screen.getByRole('dialog')).toBeInTheDocument(); // Check if modal opens
});

test('Clicking CIE button redirects correctly', () => {
  render(<Login />);
  const buttonCIE = screen.getByRole('button', { name: 'Entra con CIE' });
  fireEvent.click(buttonCIE);

  expect(global.window.location.assign).toHaveBeenCalledWith(
    `${ENV.URL_API.AUTHORIZE}?idp=${ENV.SPID_CIE_ENTITY_ID}`
  );
});

test('Clicking terms and conditions link redirects correctly', () => {
  render(<Login />);

  const termsConditionLink = screen.getByText('Termini e condizioni d’uso');
  fireEvent.click(termsConditionLink);

  expect(global.window.location.assign).toHaveBeenCalledWith(ENV.URL_FOOTER.TERMS_AND_CONDITIONS);
});

test('Clicking privacy link redirects correctly', () => {
  render(<Login />);

  const privacyLink = screen.getAllByText(/Informativa Privacy/)[0];
  fireEvent.click(privacyLink);

  expect(global.window.location.assign).toHaveBeenCalledWith(ENV.URL_FOOTER.PRIVACY_DISCLAIMER);
});
