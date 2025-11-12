import {
  PlanListSchema,
  ValidatePlanSchema,
  ValidateApiKeySchema,
  validatePlanSchema,
  ValidateError,
} from './../types/api';
import axios from 'axios';
import {
  LoginResponse,
  Client,
  clientSchema,
  ClientErrors,
  ClientWithoutSensitiveData,
} from '../types/api';
import { ENV } from '../utils/env';
import { handleApiError } from '../utils/errors';

const api = axios.create({
  headers: {
    'Content-Type': 'application/json',
  },
});

export const verifyToken = async (token: string): Promise<LoginResponse> => {
  try {
    const response = await api.get<LoginResponse>(`${ENV.URL_API.LOGIN}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    throw handleApiError(error);
  }
};

export const getClientData = async (
  token: string,
  userId?: string
): Promise<Client> => {
  if (!userId) {
    throw new Error('User ID is required');
  }
  try {
    const response = await api.get<Client>(
      `${ENV.URL_API.REGISTER}/user_id/${userId}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      throw new Error('Client not found');
    }
    if (axios.isAxiosError(error)) {
      throw new Error(
        error.response?.data?.message ||
          error.response?.data?.detail ||
          'Failed to fetch client data'
      );
    }
    throw new Error(`An unknown error occurred ${JSON.stringify(error)}`);
  }
};

export const getPlanList = async (token: string): Promise<PlanListSchema> => {
  try {
    const response = await api.get<PlanListSchema>(
      ENV.URL_API.REGISTER + ENV.URL_API.PLAN_LIST,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      throw new Error('No Plans found');
    }
    if (axios.isAxiosError(error)) {
      throw new Error(
        error.response?.data?.message ||
          error.response?.data?.detail ||
          'Failed to fetch plan data'
      );
    }
    throw new Error(`An unknown error occurred ${JSON.stringify(error)}`);
  }
};

export const createOrUpdateClient = async (
  data: ClientWithoutSensitiveData,
  token: string,
  clientId?: string,
  pairWiseData?: ValidatePlanSchema
): Promise<Client | ClientErrors> => {
  try {
    const url = clientId
      ? `${ENV.URL_API.REGISTER}/client_id/${clientId}`
      : ENV.URL_API.REGISTER;
    const method = clientId ? 'put' : 'post';

    const errors = clientSchema.safeParse(data);
    if (!errors.success) {
      return Promise.reject(errors.error.format());
    }
    const headers: Record<string, string> = {
      Authorization: `Bearer ${token}`,
      ...(pairWiseData?.apiKeyId && pairWiseData?.apiKeyValue
        ? {
            'PDV-X-Api-Key': pairWiseData.apiKeyValue,
            'PDV-Plan-Name': pairWiseData.apiKeyId,
          }
        : {}),
    };
    // mock:
    // return Promise.resolve({
    //   ...data,
    //   clientId: 'm2XC3qdG0GpSmmwoIY0NMRXiOWNDUmQyA40m7EP56bw',
    //   clientSecret: 'xxx',
    //   clientIdIssuedAt: 1234567890,
    //   clientSecretExpiresAt: 1234567890,
    // });

    // TODO: cloud we use axios middleware to inject auth bearer token ?
    // TODO: and should we use an interceptor for token expired that inform user and maybe make an automatic logout

    const response = await api[method]<Client>(url, data, {
      headers: headers,
    });
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      throw new Error(
        error.response?.data?.message || 'Failed to save client information'
      );
    }
    throw error;
  }
};

export const validateApiKeyPlan = async (
  data: ValidatePlanSchema,
  token: string
): Promise<ValidateApiKeySchema | ValidateError> => {
  try {
    const url = ENV.URL_API.REGISTER + ENV.URL_API.VALIDATE_API_PLAN;
    const method = 'post';

    const errors = validatePlanSchema.safeParse(data);
    if (!errors.success) {
      return Promise.reject(errors.error.format());
    }

    const payload = {
      api_key_id: data.apiKeyId,
      api_key_value: data.apiKeyValue,
    };

    const response = await api[method]<ValidateApiKeySchema>(url, payload, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error)) {
      throw new Error(
        error.response?.data?.message || 'Failed to validate api plan'
      );
    }
    throw error;
  }
};
