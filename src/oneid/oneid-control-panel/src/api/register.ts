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
  userId: string | undefined,
  token: string
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

export const createOrUpdateClient = async (
  data: ClientWithoutSensitiveData,
  token: string,
  clientId?: string
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
      headers: {
        Authorization: `Bearer ${token}`,
      },
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
