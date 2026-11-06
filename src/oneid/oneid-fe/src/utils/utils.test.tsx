import { describe, it, vi, expect, Mock } from 'vitest';
import { redirectToClientWithError, redirectToLoginToRetry } from './utils';
import { ERROR_CODE } from '../hooks/useLoginError';

describe('storage utilities', () => {
  // Mock localStorage and sessionStorage
  const mockStorage = {
    getItem: vi.fn(),
    length: 0,
  };
  vi.stubGlobal('window', {
    sessionStorage: mockStorage,
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should return correct route using given parameters', () => {
    const value =
      '{"scope":"openid","client_id":"client_1","state":"state","nonce":"nonce","redirect_uri":"http://example.com/cb"}';
    const redirectRetryRoute =
      '/login?scope=openid&client_id=client_1&state=state&nonce=nonce&redirect_uri=http%3A%2F%2Fexample.com%2Fcb';

    (window.sessionStorage.getItem as Mock).mockReturnValue(value);

    const result = redirectToLoginToRetry();
    expect(result).toBe(redirectRetryRoute);

    // expect(window.location.assign).toHaveBeenCalledWith(redirectRetryRoute);
  });

  it('should return null using wrong given parameters', () => {
    const value = '{}';

    (window.sessionStorage.getItem as Mock).mockReturnValue(value);

    expect(redirectToLoginToRetry()).toBeNull();
  });

  it('should return null using incomplete given parameters', () => {
    const value = '{"scope":"openid"}';

    (window.sessionStorage.getItem as Mock).mockReturnValue(value);

    expect(redirectToLoginToRetry()).toBeNull();
  });

  it('should return null using incomplete given parameters', () => {
    const value = '{"scope":"openid","client_id":"client_1"}';

    (window.sessionStorage.getItem as Mock).mockReturnValue(value);

    expect(redirectToLoginToRetry()).toBeNull();
  });

  it('should return null using wrong given parameters', () => {
    const value =
      '{"scope":"openid","client_id":"client_1","statee":"state","nonce":"nonce","redirect_uri":"http://example.com/cb"}';

    (window.sessionStorage.getItem as Mock).mockReturnValue(value);

    expect(redirectToLoginToRetry()).toBeNull();
  });

  it('should return correct route using given parameters', () => {
    const error = ERROR_CODE.CANCELED_BY_USER;
    const redirectUri = 'http://example.com/cb';
    const state = 'state';
    const expectedRoute = `http://example.com/cb?error=access_denied&error_description=${error}&state=state`;
    const result = redirectToClientWithError(error, redirectUri, state);
    expect(result).toBe(expectedRoute);
  });

  it('preserves callback query values and encodes OAuth state', () => {
    const state = 'saved state&value=1+%{token}';
    const callback = 'https://example.com/cb?source=oneid&path=%2Farea%26x%25';
    const redirect = new URL(
      redirectToClientWithError(ERROR_CODE.CANCELED_BY_USER, callback, state)
    );

    expect(redirect.searchParams.get('source')).toBe('oneid');
    expect(redirect.searchParams.get('path')).toBe('/area&x%');
    expect(redirect.searchParams.get('error')).toBe('access_denied');
    expect(redirect.searchParams.get('state')).toBe(state);
  });

  it('replaces existing OAuth parameters without duplicating them', () => {
    const redirect = new URL(
      redirectToClientWithError(
        ERROR_CODE.CANCELED_BY_USER,
        'https://example.com/cb?error=old&error_description=old&state=old',
        'saved-state'
      )
    );

    expect(redirect.searchParams.getAll('error')).toEqual(['access_denied']);
    expect(redirect.searchParams.getAll('error_description')).toEqual([
      ERROR_CODE.CANCELED_BY_USER,
    ]);
    expect(redirect.searchParams.getAll('state')).toEqual(['saved-state']);
  });

  it('keeps the legacy null state value when state is absent', () => {
    const redirect = new URL(
      redirectToClientWithError(
        ERROR_CODE.CANCELED_BY_USER,
        'https://example.com/cb?source=oneid&state=old',
        null
      )
    );

    expect(redirect.searchParams.get('source')).toBe('oneid');
    expect(redirect.searchParams.getAll('state')).toEqual(['null']);
  });
});
