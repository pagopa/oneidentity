import { describe, it, expect, vi } from 'vitest';
import { ENV } from '../utils/env';
import { setClientToUser } from './client';

vi.mock('../utils/env', () => ({
  ENV: {
    URL_API: {
      CLIENT: { USER_ATTRIBUTES: 'https://api.example.com/user-attributes' },
    },
  },
}));

const axiosMock = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
}));

vi.mock('axios', async (importActual) => {
  const actual = await importActual<typeof import('axios')>();

  return {
    default: {
      ...actual.default,
      create: vi.fn(() => ({
        ...actual.default.create(),
        get: axiosMock.get,
        post: axiosMock.post,
        put: axiosMock.put,
      })),
    },
  };
});

describe('setClientToUser', () => {
  const client_id = 'xxx';
  const user_id = 'yyy';
  const mockClientData = {
    client_id,
    user_id,
  };
  const token = 'test-token';

  it('creates a new mapping successfully', async () => {
    axiosMock.put.mockResolvedValueOnce({
      data: {
        ...mockClientData,
      },
    });

    const result = await setClientToUser(client_id, user_id, token);
    expect(result).toEqual({
      ...mockClientData,
    });
    expect(axiosMock.put).toHaveBeenCalledWith(
      `${ENV.URL_API.CLIENT.USER_ATTRIBUTES}`,
      { client_id, user_id },
      { headers: { Authorization: `Bearer ${token}` } }
    );
  });

  it('throws validation errors for invalid data', async () => {
    await expect(setClientToUser(client_id, '', token)).rejects.toThrow();
  });

  it('throws an error if the API call fails', async () => {
    axiosMock.put.mockRejectedValueOnce({
      isAxiosError: true,
      response: { data: { message: 'Failed to create mapping' } },
    });

    await expect(setClientToUser(client_id, user_id, token)).rejects.toThrow(
      'An unexpected error occurred'
    );
  });

  it('throws a generic error if the API call fails unexpectedly', async () => {
    const mockError = { isAxiosError: false, message: 'Unexpected error' };
    axiosMock.put.mockRejectedValueOnce(mockError);

    await expect(setClientToUser(client_id, user_id, token)).rejects.toThrow(
      'An unexpected error occurred'
    );
  });
});
