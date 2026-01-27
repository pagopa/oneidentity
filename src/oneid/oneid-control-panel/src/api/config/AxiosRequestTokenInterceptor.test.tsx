import { describe, it, expect, vi, beforeEach } from 'vitest';
import { AxiosHeaders, InternalAxiosRequestConfig } from 'axios';
import AxiosRequestTokenInterceptor from './AxiosRequestTokenInterceptor';
import { getIdToken } from '../../context/AuthInterceptorContext';

vi.mock('../../context/AuthInterceptorContext', () => ({
  getIdToken: vi.fn(),
}));

describe('AxiosRequestTokenInterceptor', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should add Authorization header if token exists', () => {
    const mockToken = 'test-token-abc';
    vi.mocked(getIdToken).mockReturnValue(mockToken);

    const config = {
      headers: new AxiosHeaders(),
    } as InternalAxiosRequestConfig;

    const result = AxiosRequestTokenInterceptor(config);

    expect(result.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
  });

  it('should NOT add Authorization header if token is missing', () => {
    vi.mocked(getIdToken).mockReturnValue(null);

    const config = {
      headers: new AxiosHeaders(),
    } as InternalAxiosRequestConfig;

    const result = AxiosRequestTokenInterceptor(config);

    expect(result.headers.has('Authorization')).toBe(false);
  });

  it('should return a new config object for immutability when token is added', () => {
    vi.mocked(getIdToken).mockReturnValue('some-token');

    const config = {
      headers: new AxiosHeaders({ 'Content-Type': 'application/json' }),
      url: '/test',
    } as InternalAxiosRequestConfig;

    const result = AxiosRequestTokenInterceptor(config);

    // new object (different reference)
    expect(result).not.toBe(config);
    // new headers object
    expect(result.headers).not.toBe(config.headers);
    expect(result.headers.get('Authorization')).toBe('Bearer some-token');
  });
});
