import axios from 'axios';
import { LoginResponse, ClientData } from '../types/api';
import { ENV } from '../utils/env';
import { handleApiError } from '../utils/errors';

const api = axios.create({
  baseURL: ENV.PUBLIC_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});


export const verifyToken = async (token: string): Promise<LoginResponse> => {
  try {
    const response = await api.get<LoginResponse>(`${ENV.URL_API.LOGIN}`, {
      headers: {
        'x-api-key': token,
      },
    });
    return response.data;
  } catch (error) {
    throw handleApiError(error);
  }
};

export const getClientData = async (
  clientId: string,
  token: string
): Promise<ClientData | null> => {
  try {
    const response = await api.get<ClientData>(
      `${ENV.URL_API.REGISTER}/${clientId}`,
      {
        headers: {
          'x-api-key': token,
        },
      }
    );
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      return null; // Client doesn't exist yet
    }
    throw error;
  }
};

export const createOrUpdateClient = async (
  data: Partial<Omit<ClientData, 'client_id' | 'client_secret'>>,
  token: string,
  clientId?: string
): Promise<ClientData> => {
  try {
    const url = clientId
      ? `${ENV.URL_API.REGISTER}/${clientId}`
      : ENV.URL_API.REGISTER;
    const method = clientId ? 'put' : 'post';

    const response = await api[method]<ClientData>(url, data, {
      headers: {
        'x-api-key': token,
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
