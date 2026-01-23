import { describe, it, expect, beforeEach } from 'vitest';
import { AuthContextProps } from 'react-oidc-context';
import { getAccessToken, setAuthInstance } from './AuthInterceptorContext';

describe('AuthInterceptorContext', () => {
  beforeEach(() => {
    // Reset internal state before each test
    setAuthInstance(null as unknown as AuthContextProps);
  });

  it('should return null initially', () => {
    expect(getAccessToken()).toBeNull();
  });

  it('should return the id_token when an instance is set', () => {
    const mockInstance = {
      user: {
        id_token: 'mock-id-token-123',
      },
    } as AuthContextProps;

    setAuthInstance(mockInstance);
    expect(getAccessToken()).toBe('mock-id-token-123');
  });

  it('should return null if user or id_token is missing', () => {
    const mockInstance = {
      user: null,
    } as unknown as AuthContextProps;

    setAuthInstance(mockInstance);
    expect(getAccessToken()).toBeNull();
  });

  it('should update the token if a new instance is set', () => {
    setAuthInstance({ user: { id_token: 'token-1' } } as AuthContextProps);
    expect(getAccessToken()).toBe('token-1');

    setAuthInstance({ user: { id_token: 'token-2' } } as AuthContextProps);
    expect(getAccessToken()).toBe('token-2');
  });
});
