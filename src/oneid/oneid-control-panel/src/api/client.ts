import {
  IdpUser,
  IdpUserCreateOrUpdateResponse,
  IdpUserList,
} from './../types/api';
import axios from 'axios';
import { ENV } from '../utils/env';
import { handleApiError } from '../utils/errors';

const api = axios.create({
  headers: {
    'Content-Type': 'application/json',
  },
});

const userIdMessage = 'User ID is required';

export const getClientUsers = async (
  userId: string | undefined,
  token: string
): Promise<IdpUserList> => {
  const ENDPOINT = ENV.URL_API.CLIENT.CLIENT_USERS;

  if (!userId) {
    throw new Error(userIdMessage);
  }
  try {
    // TODO: remove 1000 and implement server pagination
    const response = await api.get<IdpUserList>(
      `${ENDPOINT}/${userId}?limit=1000`,
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

export const deleteClientUser = async (
  userId: string | undefined,
  token: string,
  username: string | undefined
): Promise<void> => {
  const ENDPOINT = ENV.URL_API.CLIENT.CLIENT_USERS;

  if (!userId || !username) {
    throw new Error(userIdMessage);
  }
  try {
    await api.delete<string>(`${ENDPOINT}/${userId}/${username}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  } catch (error) {
    throw handleApiError(error);
  }
};

export const updateClientUser = async (
  userId: string | undefined,
  username: string | undefined,
  data: IdpUser,
  token: string
): Promise<IdpUserCreateOrUpdateResponse> => {
  const ENDPOINT = ENV.URL_API.CLIENT.CLIENT_USERS;

  if (!userId || !username) {
    throw new Error(userIdMessage);
  }
  try {
    const response = await api.patch<IdpUserCreateOrUpdateResponse>(
      `${ENDPOINT}/${userId}/${username}`,
      data,
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

export const addClientUser = async (
  data: IdpUser,
  token: string
): Promise<IdpUserCreateOrUpdateResponse> => {
  const ENDPOINT = ENV.URL_API.CLIENT.CLIENT_USERS;

  if (!data) {
    throw new Error('Data is required');
  }
  try {
    const response = await api.post<IdpUserCreateOrUpdateResponse>(
      `${ENDPOINT}`,
      data,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 409) {
      throw new Error('User already exists');
    }
    throw handleApiError(error);
  }
};
