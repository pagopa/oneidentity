import { describe, it, expect, vi, beforeEach } from 'vitest';
import { getClientData, createOrUpdateClient } from './register';
import { ENV } from '../utils/env';
import { Client, SamlAttribute, SpidLevel } from '../types/api';

vi.mock('../utils/env', () => ({
  ENV: { URL_API: { REGISTER: 'https://api.example.com/register' } },
}));

const axiosMock = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  patch: vi.fn(),
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
        patch: axiosMock.patch,
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
    backButtonEnabled: true,
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
      'User ID is required'
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
  const mockClientData: Omit<Client, 'clientId' | 'clientSecret'> = {
    clientName: 'Test Client',
    redirectUris: ['https://example.com/callback'],
    logoUri: 'https://example.com/logo.png',
    policyUri: 'https://example.com/policy',
    tosUri: 'https://example.com/tos',
    defaultAcrValues: [SpidLevel.L2],
    samlRequestedAttributes: [SamlAttribute.FISCAL_NUMBER],
    backButtonEnabled: false, // aggiungi il default manualmente
  };

  const token = 'test-token';

  it('creates a new client successfully', async () => {
    axiosMock.post.mockResolvedValueOnce({
      data: {
        ...mockClientData,
      },
    });

    const result = await createOrUpdateClient(mockClientData, token);
    expect(result).toEqual({
      ...mockClientData,
    });
    expect(axiosMock.post).toHaveBeenCalledWith(
      `${ENV.URL_API.REGISTER}`,
      mockClientData,
      { headers: { Authorization: `Bearer ${token}` } }
    );
  });

  it('updates an existing client successfully', async () => {
    const clientId = 'existing-client-id';
    axiosMock.patch.mockResolvedValue({
      status: 204,
      data: null,
    });

    const result = await createOrUpdateClient(mockClientData, token, clientId);
    expect(result).toBeNull();
    expect(axiosMock.patch).toHaveBeenCalledWith(
      `${ENV.URL_API.REGISTER}/client_id/${clientId}`,
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
