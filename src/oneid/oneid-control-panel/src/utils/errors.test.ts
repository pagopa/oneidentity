import { AxiosError } from 'axios';
import { ApiError, CorsError, handleApiError } from './errors';

describe('handleApiError', () => {
  it('should handle CORS errors', () => {
    const error = new AxiosError('Network Error', 'ERR_NETWORK');
    const result = handleApiError(error);
    expect(result).toBeInstanceOf(Error);
    expect(result.message).toBe(
      'CORS error: Unable to access the API. Please check your configuration.'
    );
  });

  it('should handle network errors', () => {
    const error = new Error('Network Error');
    const result = handleApiError(error);
    expect(result).toBeInstanceOf(Error);
    expect(result.message).toBe(
      'Network error. Please check your internet connection.'
    );
  });

  it('should handle timeout errors', () => {
    const error = new Error('timeout');
    const result = handleApiError(error);
    expect(result).toBeInstanceOf(Error);
    expect(result.message).toBe(
      'Request timed out. Please check your network connection.'
    );
  });

  it('should pass through other errors', () => {
    const error = new Error('Some other error');
    const result = handleApiError(error);
    expect(result).toBe(error);
  });

  it('should handle unexpected errors', () => {
    const error = 'unexpected error';
    const result = handleApiError(error);
    expect(result).toBeInstanceOf(Error);
    expect(result.message).toBe('An unexpected error occurred');
  });
});

describe('ApiError', () => {
  it('should create an instance of ApiError', () => {
    const error = new ApiError('API Error', 404, 'Not Found');
    expect(error).toBeInstanceOf(ApiError);
    expect(error.message).toBe('API Error');
    expect(error.status).toBe(404);
    expect(error.statusText).toBe('Not Found');
    expect(error.name).toBe('ApiError');
  });
});

describe('CorsError', () => {
  it('should create an instance of CorsError with a default message', () => {
    const error = new CorsError();
    expect(error).toBeInstanceOf(CorsError);
    expect(error.message).toBe('CORS request failed');
    expect(error.name).toBe('CorsError');
  });

  it('should create an instance of CorsError with a custom message', () => {
    const error = new CorsError('Custom CORS message');
    expect(error).toBeInstanceOf(CorsError);
    expect(error.message).toBe('Custom CORS message');
    expect(error.name).toBe('CorsError');
  });
});
