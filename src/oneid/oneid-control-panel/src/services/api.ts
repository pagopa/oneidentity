import axios from 'axios';
import { LoginResponse, Client } from '../types/api';
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
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    throw handleApiError(error);
  }
};

export const getClientData = async (
  clientId: string | undefined,
  token: string
): Promise<Client> => {
  if (!clientId) {
    throw new Error('Client ID is required');
  }
  const out =
    '{"redirect_uris":["https://442zl6z6sbdqprefkazmp6dr3y0nmnby.lambda-url.eu-south-1.on.aws/client/cb"],"client_name":"cognito_METADATA_07_01_122456","logo_uri":"http://test.com/logo.png","policy_uri":null,"tos_uri":null,"default_acr_values":["https://www.spid.gov.it/SpidL2"],"saml_requested_attributes":["fiscalNumber"]}';
  return JSON.parse(out);
  try {
    const response = await api.get<Client>(
      `${ENV.URL_API.REGISTER}/${clientId}`,
      {
        withCredentials: false,
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
        error.response?.data?.message || 'Failed to fetch client data'
      );
    }
    throw new Error('An unknown error occurred');
  }
};

const isInvalidUrl = (url: string) => {
  try {
    new URL(url);
    return false;
  } catch (e) {
    return true;
  }
};

const simulateErrors = (c: Omit<Client, 'client_id' | 'client_secret'>) => {
  const errors = {};
  if (c.client_name === '') {
    errors.client_name = 'Client name is required';
  }
  // if url is present and valid
  if (c.redirect_uris.length === 0 || c.redirect_uris.some(isInvalidUrl)) {
    errors.redirect_uris = 'At least one VALID redirect URI is required';
  }
  if (c.default_acr_values.length === 0) {
    errors.default_acr_values = 'At least one default ACR value is required';
  }
  if (c.saml_requested_attributes.length === 0) {
    errors.saml_requested_attributes =
      'At least one SAML requested attribute is required';
  }
  if (c.logo_uri && isInvalidUrl(c.logo_uri)) {
    errors.logo_uri = 'Logo URI is invalid';
  }
  if (c.policy_uri && isInvalidUrl(c.policy_uri)) {
    errors.policy_uri = 'Policy URI is invalid';
  }
  if (c.tos_uri && isInvalidUrl(c.tos_uri)) {
    errors.tos_uri = 'TOS URI is invalid';
  }
  return errors;
};

export const createOrUpdateClient = async (
  data: Omit<Client, 'client_id' | 'client_secret'>,
  token: string,
  clientId?: string
): Promise<Client> => {
  try {
    const url = clientId
      ? `${ENV.URL_API.REGISTER}/${clientId}`
      : ENV.URL_API.REGISTER;
    const method = clientId ? 'put' : 'post';
    console.log('Creating or updating client:', data, clientId);

    const errors = simulateErrors(data);
    if (Object.keys(errors).length > 0) {
      return Promise.reject(simulateErrors(data));
    }

    return Promise.resolve({
      ...data,
      client_id: clientId || 'xxx',
      client_secret: 'xxx',
      client_id_issued_at: 1234567890,
      client_secret_expires_at: 1234567890,
    });

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
