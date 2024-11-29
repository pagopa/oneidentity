/* eslint-disable functional/immutable-data */
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { afterAll, beforeAll, afterEach, test, vi, Mock } from 'vitest';
import { ENV } from '../../utils/env';
import { i18nTestSetup } from '../../__tests__/i18nTestSetup';
import Login from '.';

// Constants for repeated strings
const SPID_LOGIN = 'spidButton';
const CIE_LOGIN = 'CIE Login';
const LOGIN_TITLE = 'Login Title';
const LOGIN_DESCRIPTION = 'Login Description';
const TEMPORARY_LOGIN_ALERT = 'Temporary Login Alert';
const ALERT_DESCRIPTION = 'This is a warning!';
const MOCK_IDP_ENTITY_ID = 'test-idp';
const MOCK_RICHIEDI_SPID_URL = 'https://example.com/spid';

// Setup translations
i18nTestSetup({
  loginPage: {
    title: LOGIN_TITLE,
    description: LOGIN_DESCRIPTION,
    loginBox: {
      spidLogin: SPID_LOGIN,
      cieLogin: CIE_LOGIN,
    },
    privacyAndCondition: {
      text: 'terms: {{termsLink}} privacy: {{privacyLink}}',
    },
    temporaryLogin: {
      alert: TEMPORARY_LOGIN_ALERT,
      join: 'Join',
    },
  },
  spidSelect: {
    modalTitle: 'test modal',
  },
});

// Clear mocks after each test
afterEach(() => {
  vi.clearAllMocks();
});

beforeEach(() => {
  // Mock fetch
  global.fetch = vi.fn();
});

const oldWindowLocation = global.window.location;

beforeAll(() => {
  // Mock window location
  Object.defineProperty(window, 'location', { value: { assign: vi.fn() } });
});

afterAll(() => {
  Object.defineProperty(window, 'location', { value: oldWindowLocation });
});

// Test cases
test('Renders Login component', () => {
  render(<Login />);
  expect(screen.getByText(LOGIN_TITLE)).toBeInTheDocument();
  expect(screen.getByText(LOGIN_DESCRIPTION)).toBeInTheDocument();
});

test('Fetches and displays banner alerts', async () => {
  const mockBannerResponse = [
    { enable: true, severity: 'warning', description: ALERT_DESCRIPTION },
  ];
  (fetch as Mock).mockResolvedValueOnce({
    json: vi.fn().mockResolvedValueOnce(mockBannerResponse),
  });

  render(<Login />);

  await waitFor(() => {
    expect(screen.getByText(ALERT_DESCRIPTION)).toBeInTheDocument();
  });
});

test('Handles fetch error for alert message', async () => {
  (fetch as Mock).mockRejectedValueOnce(new Error('Fetch failed'));

  render(<Login />);

  await waitFor(() => {
    expect(screen.queryByText(ALERT_DESCRIPTION)).not.toBeInTheDocument();
  });
});

test('Fetches IDP list on mount', async () => {
  const mockIDPListResponse = {
    identityProviders: [{ entityID: MOCK_IDP_ENTITY_ID }],
    richiediSpid: MOCK_RICHIEDI_SPID_URL,
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
  window.history.pushState({}, '', '?client_id=invalidId');

  render(<Login />);

  await waitFor(() => {
    expect(screen.queryByAltText('Test Client')).not.toBeInTheDocument();
  });
});

test('Clicking CIE button redirects correctly', () => {
  render(<Login />);
  const buttonCIE = screen.getByText(CIE_LOGIN);
  fireEvent.click(buttonCIE);

  expect(global.window.location.assign).toHaveBeenCalledWith(
    `${ENV.URL_API.AUTHORIZE}?idp=${ENV.SPID_CIE_ENTITY_ID}`
  );
});

test('Displays temporary login alert if enabled', () => {
  render(<Login />);
  const temporaryLoginAlert = screen.getByText(TEMPORARY_LOGIN_ALERT);
  expect(temporaryLoginAlert).toBeInTheDocument();
});
