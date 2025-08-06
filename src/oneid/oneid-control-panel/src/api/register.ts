import axios from 'axios';
import {
  LoginResponse,
  Client,
  clientSchema,
  ClientErrors,
} from '../types/api';
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
  userId: string | undefined,
  token: string
): Promise<Client> => {
  if (!clientId) {
    throw new Error('Client ID is required');
  }
  // mock:
  // const out =
  //   '{"redirect_uris":["https://442zl6z6sbdqprefkazmp6dr3y0nmnby.lambda-url.eu-south-1.on.aws/client/cb"],"client_name":"cognito_METADATA_07_01_122456","logo_uri":"http://test.com/logo.png","policy_uri":null,"tos_uri":null,"default_acr_values":["https://www.spid.gov.it/SpidL2"],"saml_requested_attributes":["fiscalNumber"]}';
  // return JSON.parse(out);
  try {
    const response = await api.get<Client>(
      `${ENV.URL_API.REGISTER}/${clientId}/${userId}`,
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
        error.response?.data?.message || 'Failed to fetch client data'
      );
    }
    throw new Error(`An unknown error occurred ${JSON.stringify(error)}`);
  }
};

export const createOrUpdateClient = async (
  data: Omit<Client, 'client_id' | 'client_secret'>,
  token: string,
  clientId?: string
): Promise<Client | ClientErrors> => {
  try {
    const url = clientId
      ? `${ENV.URL_API.REGISTER}/${clientId}`
      : ENV.URL_API.REGISTER;
    const method = clientId ? 'put' : 'post';

    const errors = clientSchema.safeParse(data);
    if (!errors.success) {
      return Promise.reject(errors.error.format());
    }

    // mock:
    // return Promise.resolve({
    //   ...data,
    //   client_id: 'm2XC3qdG0GpSmmwoIY0NMRXiOWNDUmQyA40m7EP56bw',
    //   client_secret: 'xxx',
    //   client_id_issued_at: 1234567890,
    //   client_secret_expires_at: 1234567890,
    // });

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
