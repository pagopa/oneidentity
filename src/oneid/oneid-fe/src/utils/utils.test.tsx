import { describe, it, vi, expect, Mock } from 'vitest';
import { redirectToLoginToRetry } from './utils';

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
});
