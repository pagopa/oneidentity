import axios from 'axios';
import { ENV } from '../utils/env';
import { handleApiError } from '../utils/errors';

const ENDPOINT = ENV.URL_API.CLIENT.USER_ATTRIBUTES;
const api = axios.create({
  baseURL: ENDPOINT,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const setClientToUser = async (
  clientId: string | undefined,
  userId: string | undefined,
  token: string
) => {
  if (!clientId && !userId) {
    throw new Error('Client ID and User ID are required');
  }
  try {
    const response = await api.put<string>(`${ENDPOINT}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
      data: {
        client_id: clientId,
        user_id: userId,
      },
    });
    return response.data;
  } catch (error) {
    throw handleApiError(error);
  }
};
