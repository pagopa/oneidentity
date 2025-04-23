import { describe, it, expect, vi, beforeEach } from 'vitest';
import { getClientData, createOrUpdateClient } from './api';
import { ENV } from '../utils/env';
import { SamlAttribute, SpidLevel } from '../types/api';

vi.mock('../utils/env', () => ({
  ENV: { URL_API: { REGISTER: 'https://api.example.com/register' } },
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

describe('getClientData', () => {
  const clientID = '0000000000000000000000000000000000000000000';

  const mockClientData = {
    client_id: clientID,
    client_name: 'Test Client',
    logo_uri: 'https://example.com/logo.png',
    policy_uri: 'https://example.com/policy',
    tos_uri: 'https://example.com/tos',
  };

  beforeEach(() => {
    vi.stubGlobal('window', {
      location: { search: `?client_id=${clientID}` },
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('fetches client data successfully', async () => {
    axiosMock.get.mockResolvedValueOnce({
      data: mockClientData,
    });

    const result = await getClientData('client_id', 'token');
    expect(result).toEqual(mockClientData);
  });

  it('throws an error if client_id is invalid or missing', async () => {
    await expect(getClientData('', 'token')).rejects.toThrow(
      'Client ID is required'
    );
  });

  it('throws a custom error if the fetch fails', async () => {
    axiosMock.get.mockRejectedValue({
      isAxiosError: true,
      response: { data: { status: 401, message: 'Unauthorized' } },
    });

    await expect(getClientData('client_id', 'token')).rejects.toThrow(
      'Unauthorized'
    );
  });

  it('throws an error if the fetch fails', async () => {
    axiosMock.get.mockRejectedValue({
      isAxiosError: true,
      response: { data: { status: 401 } },
    });

    await expect(getClientData('client_id', 'token')).rejects.toThrow(
      'Failed to fetch client data'
    );
  });
  it('throws a generic error if the fetch fails', async () => {
    const mockError = {
      isAxiosError: false,
      response: { data: { status: 401 } },
    };
    axiosMock.get.mockRejectedValue(mockError);

    await expect(getClientData('client_id', 'token')).rejects.toThrow(
      `An unknown error occurred ${JSON.stringify(mockError)}`
    );
  });
});

describe('createOrUpdateClient', () => {
  const mockClientData = {
    client_name: 'Test Client',
    redirect_uris: ['https://example.com/callback'],
    logo_uri: 'https://example.com/logo.png',
    policy_uri: 'https://example.com/policy',
    tos_uri: 'https://example.com/tos',
    default_acr_values: [SpidLevel.L2],
    saml_requested_attributes: [SamlAttribute.FISCAL_NUMBER],
  };

  const token = 'test-token';

  it('creates a new client successfully', async () => {
    axiosMock.post.mockResolvedValueOnce({
      data: {
        ...mockClientData,
        client_id: 'new-client-id',
        client_secret: 'new-client-secret',
      },
    });

    const result = await createOrUpdateClient(mockClientData, token);
    expect(result).toEqual({
      ...mockClientData,
      client_id: 'new-client-id',
      client_secret: 'new-client-secret',
    });
    expect(axiosMock.post).toHaveBeenCalledWith(
      `${ENV.URL_API.REGISTER}`,
      mockClientData,
      { headers: { Authorization: `Bearer ${token}` } }
    );
  });

  it('updates an existing client successfully', async () => {
    const clientId = 'existing-client-id';
    axiosMock.put.mockResolvedValueOnce({
      data: {
        ...mockClientData,
        client_id: clientId,
        client_secret: 'existing-client-secret',
      },
    });

    const result = await createOrUpdateClient(mockClientData, token, clientId);
    expect(result).toEqual({
      ...mockClientData,
      client_id: clientId,
      client_secret: 'existing-client-secret',
    });
    expect(axiosMock.put).toHaveBeenCalledWith(
      `${ENV.URL_API.REGISTER}/${clientId}`,
      mockClientData,
      { headers: { Authorization: `Bearer ${token}` } }
    );
  });

  it('throws validation errors for invalid client data', async () => {
    const invalidData = { ...mockClientData, client_name: '' }; // Invalid client_name
    await expect(createOrUpdateClient(invalidData, token)).rejects.toThrow();
  });

  it('throws an error if the API call fails', async () => {
    axiosMock.post.mockRejectedValueOnce({
      isAxiosError: true,
      response: { data: { message: 'Failed to create client' } },
    });

    await expect(createOrUpdateClient(mockClientData, token)).rejects.toThrow(
      'Failed to create client'
    );
  });

  it('throws a generic error if the API call fails unexpectedly', async () => {
    const mockError = { isAxiosError: false, message: 'Unexpected error' };
    axiosMock.post.mockRejectedValueOnce(mockError);

    await expect(createOrUpdateClient(mockClientData, token)).rejects.toThrow(
      'Unexpected error'
    );
  });
});
