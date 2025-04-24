import axios from 'axios';
import { ENV } from '../utils/env';
import { handleApiError } from '../utils/errors';

const api = axios.create({
  baseURL: ENV.PUBLIC_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const setCognitoParam = async (
  clientId: string | undefined,
  token: string
) => {
  if (!clientId) {
    throw new Error('Client ID is required');
  }
  try {
    const response = await api.get<string>(
      `${ENV.URL_API.REGISTER}/${clientId}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return response.data;
  } catch (error) {
    throw handleApiError(error);
  }
};
