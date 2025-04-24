/* eslint-disable sonarjs/no-duplicate-string */
import { AxiosError } from 'axios';

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public statusText: string
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export class CorsError extends Error {
  constructor(message = 'CORS request failed') {
    super(message);
    this.name = 'CorsError';
  }
}

export const handleApiError = (error: unknown): Error => {
  if (error instanceof Error) {
    // Handle CORS errors
    if (error.message.includes('Network Error') && isCorsError(error)) {
      return new Error(
        'CORS error: Unable to access the API. Please check your configuration.'
      );
    }

    // Handle actual network errors
    if (error.message.includes('Network Error')) {
      return new Error('Network error. Please check your internet connection.');
    }

    // Handle timeout errors
    if (error.message.includes('timeout')) {
      return new Error(
        'Request timed out. Please check your network connection.'
      );
    }

    // Pass through other errors
    return error;
  }

  return new Error('An unexpected error occurred');
};

function isCorsError(error: Error): boolean {
  if (error instanceof AxiosError) {
    // CORS errors typically have no response and a specific error code
    return (
      !error.response &&
      error.code === 'ERR_NETWORK' &&
      error.message.includes('Network Error')
    );
  }
  return false;
}
