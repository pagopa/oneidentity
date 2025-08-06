import {
  IdpUser,
  IdpUserCreateOrUpdateResponse,
  IdpUserList,
} from './../types/api';
import axios from 'axios';
import { ENV } from '../utils/env';
import { handleApiError } from '../utils/errors';
import { ClientFE, ClientFEErrors, clientFESchema } from '../types/api';

const api = axios.create({
  headers: {
    'Content-Type': 'application/json',
  },
});

const userIdMessage = 'User ID is required';

export const setClientToUser = async (
  clientId: string | undefined,
  userId: string | undefined,
  token: string
) => {
  const ENDPOINT = ENV.URL_API.CLIENT.USER_ATTRIBUTES;

  if (!clientId && !userId) {
    throw new Error('Client ID and User ID are required');
  }
  // mock:
  // return Promise.resolve(true);
  const data = {
    client_id: clientId,
    user_id: userId,
  };

  try {
    const response = await api.put<string>(`${ENDPOINT}`, data, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    throw handleApiError(error);
  }
};

export const getAdditionalClientAttributes = async (
  userId: string | undefined,
  token: string
): Promise<ClientFE> => {
  const ENDPOINT = ENV.URL_API.CLIENT.CLIENT_ADDITIONAL;

  if (!userId) {
    throw new Error(userIdMessage);
  }
  // mock: no additional attributes
  // return Promise.resolve({
  //   a11yUri: null,
  //   backButtonEnabled: false,
  //   localizedContentMap: null,
  // });

  // mock: additional attributes exist
  // return Promise.resolve({
  //   a11yUri: 'https://example.com/a11y',
  //   backButtonEnabled: true,
  //   localizedContentMap: {
  //     default: {
  //       it: {
  //         title: 'Default Title',
  //         description: 'Default Description',
  //         docUri: 'https://example.com/doc',
  //         cookieUri: 'https://example.com/cookie',
  //         supportAddress: '',
  //       },
  //     },
  //     test: {
  //       en: {
  //         title: 'enDefault Title',
  //         description: 'enDefault Description',
  //         docUri: 'enhttps://example.com/doc',
  //         cookieUri: 'enhttps://example.com/cookie',
  //         supportAddress: '',
  //       },
  //     },
  //   },
  // });
  try {
    const response = await api.get<ClientFE>(`${ENDPOINT}/${userId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    throw handleApiError(error);
  }
};

export const setAdditionalClientAttributes = async (
  userId: string | undefined,
  data: ClientFE,
  token: string
): Promise<null | ClientFEErrors> => {
  const ENDPOINT = ENV.URL_API.CLIENT.CLIENT_ADDITIONAL;
  const method = userId ? 'put' : 'post';

  const errors = clientFESchema.safeParse(data);
  if (!errors.success) {
    return Promise.reject(errors.error.format());
  }
  if (!userId) {
    throw new Error(userIdMessage);
  }
  // mock:
  // return Promise.resolve(data);
  try {
    const response = await api[method]<null>(`${ENDPOINT}/${userId}`, data, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  } catch (error) {
    throw handleApiError(error);
  }
};

export const getClientUsers = async (
  userId: string | undefined,
  token: string
): Promise<IdpUserList> => {
  const ENDPOINT = ENV.URL_API.CLIENT.CLIENT_USERS;

  if (!userId) {
    throw new Error(userIdMessage);
  }
  try {
    const response = await api.get<IdpUserList>(`${ENDPOINT}/${userId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
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
    const response = await api.put<IdpUserCreateOrUpdateResponse>(
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
    throw handleApiError(error);
  }
};
