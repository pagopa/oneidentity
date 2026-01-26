import {
  IdpUser,
  IdpUserCreateOrUpdateResponse,
  IdpUserList,
} from './../types/api';
import axios from 'axios';
import api from './config/AxiosBase';
import { ENV } from '../utils/env';
import { handleApiError } from '../utils/errors';

const userNameMessage = 'User Name is required';

export const getClientUsers = async (): Promise<IdpUserList> => {
  const ENDPOINT = ENV.URL_API.CLIENT_USERS;

  try {
    // TODO: remove 1000 and implement server pagination
    const response = await api.get<IdpUserList>(`${ENDPOINT}?limit=1000`);
    return response.data;
  } catch (error) {
    throw handleApiError(error);
  }
};

export const deleteClientUser = async (
  username: string | undefined
): Promise<void> => {
  const ENDPOINT = ENV.URL_API.CLIENT_USERS;

  if (!username) {
    throw new Error(userNameMessage);
  }
  try {
    await api.delete<string>(`${ENDPOINT}/${username}`);
  } catch (error) {
    throw handleApiError(error);
  }
};

export const updateClientUser = async (
  username: string | undefined,
  data: IdpUser
): Promise<IdpUserCreateOrUpdateResponse> => {
  const ENDPOINT = ENV.URL_API.CLIENT_USERS;

  if (!username) {
    throw new Error(userNameMessage);
  }
  try {
    const response = await api.patch<IdpUserCreateOrUpdateResponse>(
      `${ENDPOINT}/${username}`,
      data
    );
    return response.data;
  } catch (error) {
    throw handleApiError(error);
  }
};

export const addClientUser = async (
  data: IdpUser
): Promise<IdpUserCreateOrUpdateResponse> => {
  const ENDPOINT = ENV.URL_API.CLIENT_USERS;

  if (!data) {
    throw new Error('Data is required');
  }
  try {
    const response = await api.post<IdpUserCreateOrUpdateResponse>(
      `${ENDPOINT}`,
      data
    );
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 409) {
      throw new Error('User already exists');
    }
    throw handleApiError(error);
  }
};
