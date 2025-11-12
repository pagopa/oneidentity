import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  getClientData,
  createOrUpdateClient,
  getPlanList,
  validateApiKeyPlan,
} from './register';
import { ENV } from '../utils/env';
import {
  Client,
  SamlAttribute,
  SpidLevel,
  ValidateApiKeySchema,
  ValidatePlanSchema,
} from '../types/api';

vi.mock('../utils/env', () => ({
  ENV: { URL_API: { REGISTER: 'https://api.example.com/register' } },
}));

const unexpectedError = 'Unexpected error';

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

  it('fetches plan data successfully', async () => {
    axiosMock.get.mockResolvedValueOnce({
      data: mockClientData,
    });

    const result = await getClientData('token', 'user_id');
    expect(result).toEqual(mockClientData);
  });

  it('throws an error if client_id is invalid or missing', async () => {
    await expect(getClientData('token')).rejects.toThrow('User ID is required');
  });

  it('throws a custom error if the fetch fails', async () => {
    axiosMock.get.mockRejectedValue({
      isAxiosError: true,
      response: { data: { status: 401, message: 'Unauthorized' } },
    });

    await expect(getClientData('token', 'user_id')).rejects.toThrow(
      'Unauthorized'
    );
  });

  it('throws an error if the fetch fails', async () => {
    axiosMock.get.mockRejectedValue({
      isAxiosError: true,
      response: { data: { status: 401 } },
    });

    await expect(getClientData('token', 'user_id')).rejects.toThrow(
      'Failed to fetch client data'
    );
  });
  it('throws a generic error if the fetch fails', async () => {
    const mockError = {
      isAxiosError: false,
      response: { data: { status: 401 } },
    };
    axiosMock.get.mockRejectedValue(mockError);

    await expect(getClientData('token', 'user_id')).rejects.toThrow(
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
    axiosMock.put.mockResolvedValueOnce({
      data: {
        ...mockClientData,
      },
    });

    const result = await createOrUpdateClient(mockClientData, token, clientId);
    expect(result).toEqual({
      ...mockClientData,
    });
    expect(axiosMock.put).toHaveBeenCalledWith(
      `${ENV.URL_API.REGISTER}/client_id/${clientId}`,
      mockClientData,
      { headers: { Authorization: `Bearer ${token}` } }
    );
  });

  it('updates an existing client successfully pairwise enabled', async () => {
    const clientId = 'existing-client-id';
    axiosMock.put.mockResolvedValueOnce({
      data: {
        ...mockClientData,
        pairwise: true,
      },
    });

    const mockValidatePlanSchema: ValidatePlanSchema = {
      apiKeyId: 'plan1',
      apiKeyValue: 'keyValue',
    };

    const result = await createOrUpdateClient(
      mockClientData,
      token,
      clientId,
      mockValidatePlanSchema
    );
    expect(result).toEqual({
      ...mockClientData,
      pairwise: true,
    });
    expect(axiosMock.put).toHaveBeenCalledWith(
      `${ENV.URL_API.REGISTER}/client_id/${clientId}`,
      mockClientData,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          'PDV-X-Api-Key': mockValidatePlanSchema.apiKeyValue,
          'PDV-Plan-Name': mockValidatePlanSchema.apiKeyId,
        },
      }
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
    const mockError = { isAxiosError: false, message: unexpectedError };
    axiosMock.post.mockRejectedValueOnce(mockError);

    await expect(createOrUpdateClient(mockClientData, token)).rejects.toThrow(
      unexpectedError
    );
  });
});
describe('getPlanData', () => {
  const clientID = '0000000000000000000000000000000000000000000';

  const mockPlanData = {
    api_keys: [
      {
        id: 'id1',
        name: 'plan1',
      },
      {
        id: 'id2',
        name: 'plan2',
      },
    ],
  };

  beforeEach(() => {
    vi.stubGlobal('window', {
      location: { search: `?client_id=${clientID}` },
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('fetches plan data successfully', async () => {
    axiosMock.get.mockResolvedValueOnce({
      data: mockPlanData,
    });

    const result = await getPlanList('token');
    expect(result).toEqual(mockPlanData);
  });

  it('throws a custom error if the fetch fails', async () => {
    axiosMock.get.mockRejectedValue({
      isAxiosError: true,
      response: { data: { status: 401, message: 'Unauthorized' } },
    });

    await expect(getPlanList('token')).rejects.toThrow('Unauthorized');
  });

  it('throws an error if the fetch fails', async () => {
    axiosMock.get.mockRejectedValue({
      isAxiosError: true,
      response: { data: { status: 401 } },
    });

    await expect(getPlanList('token')).rejects.toThrow(
      'Failed to fetch plan data'
    );
  });
  it('throws a generic error if the fetch fails', async () => {
    const mockError = {
      isAxiosError: false,
      response: { data: { status: 401 } },
    };
    axiosMock.get.mockRejectedValue(mockError);

    await expect(getPlanList('token')).rejects.toThrow(
      `An unknown error occurred ${JSON.stringify(mockError)}`
    );
  });
});

describe('validateApiKeyPlan', () => {
  const mockValidateApiKeyData: ValidateApiKeySchema = {
    valid: true,
  };

  const mockValidatePlan: ValidatePlanSchema = {
    apiKeyId: 'plan1',
    apiKeyValue: 'keyValue',
  };

  const token = 'test-token';

  it('validates key successfully', async () => {
    axiosMock.post.mockResolvedValueOnce({
      data: {
        ...mockValidateApiKeyData,
      },
    });

    const result = await validateApiKeyPlan(mockValidatePlan, token);
    expect(result).toEqual({
      ...mockValidateApiKeyData,
    });
    expect(axiosMock.post).toHaveBeenCalledWith(
      ENV.URL_API.REGISTER + ENV.URL_API.VALIDATE_API_PLAN,
      {
        api_key_id: mockValidatePlan.apiKeyId,
        api_key_value: mockValidatePlan.apiKeyValue,
      },
      { headers: { Authorization: `Bearer ${token}` } }
    );
  });

  it('throws validation errors for invalid API key data', async () => {
    const invalidData = { ...mockValidatePlan, apiKeyValue: '' };
    await expect(validateApiKeyPlan(invalidData, token)).rejects.toThrow();
  });

  it('throws an error if the API call fails', async () => {
    axiosMock.post.mockRejectedValueOnce({
      isAxiosError: true,
      response: { data: { message: 'Failed to validate API key' } },
    });

    await expect(validateApiKeyPlan(mockValidatePlan, token)).rejects.toThrow(
      'Failed to validate API key'
    );
  });

  it('throws a generic error if the API call fails unexpectedly', async () => {
    const mockError = { isAxiosError: false, message: unexpectedError };
    axiosMock.post.mockRejectedValueOnce(mockError);

    await expect(validateApiKeyPlan(mockValidatePlan, token)).rejects.toThrow(
      unexpectedError
    );
  });
});
