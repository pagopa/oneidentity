import {
  PlanListSchema,
  ValidatePlanSchema,
  ValidateApiKeySchema,
  validatePlanSchema,
  ValidateError,
} from './../types/api';
import axios from 'axios';
import api from './config/AxiosBase';
import {
  Client,
  clientSchema,
  ClientErrors,
  ClientWithoutSensitiveData,
} from '../types/api';
import { ENV } from '../utils/env';

export const getClientData = async (userId?: string): Promise<Client> => {
  if (!userId) {
    throw new Error('User ID is required');
  }
  try {
    const response = await api.get<Client>(
      `${ENV.URL_API.REGISTER}/user_id/${userId}`
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

export const getPlanList = async (): Promise<PlanListSchema> => {
  try {
    const response = await api.get<PlanListSchema>(
      ENV.URL_API.REGISTER + ENV.URL_API.PLAN_LIST
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
      ...(pairWiseData?.apiKeyId && pairWiseData?.apiKeyValue
        ? {
            'Plan-Api-Key': pairWiseData.apiKeyValue,
            'Plan-Name': pairWiseData.apiKeyId,
          }
        : {}),
    };

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
  data: ValidatePlanSchema
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

    const response = await api[method]<ValidateApiKeySchema>(url, payload);
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
