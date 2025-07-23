import { UserApi } from './../types/api';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useAuth } from 'react-oidc-context';
import {
  getAdditionalClientAttributes,
  setAdditionalClientAttributes,
  setClientToUser,
  addClientUser,
  updateClientUser,
  deleteClientUser,
  getClientUsers,
} from '../api/client';
import { useParams } from 'react-router-dom';
import { ClientFE } from '../types/api';

const retry = 2;

export const useClient = () => {
  const { user } = useAuth();
  const token = user?.id_token;
  const userId = user?.profile.sub;

  const { client_id } = useParams(); // Get the client_id from the URL
  if (!token) {
    throw new Error('No token available');
  }

  const setCognitoProfile = useMutation({
    onError(error) {
      console.error('Error creating or updating client:', error);
    },
    mutationFn: async ({ clientId }: { clientId: string | undefined }) => {
      if (!clientId && !userId) {
        throw new Error('Client ID and User ID are required');
      }

      return await setClientToUser(clientId, userId, token);
    },
    retry,
  });

  const getAdditionalClientAttrs = useQuery({
    queryKey: ['client', 'additional', client_id],
    queryFn: async () => {
      if (!userId) {
        throw new Error('userId is required');
      }
      return await getAdditionalClientAttributes(userId, token);
    },
    retry,
    enabled: !!userId && !!token && !!client_id,
  });

  const createOrUpdateClientAttrsMutation = useMutation({
    onError(error) {
      console.error('Error creating or updating client:', error);
    },
    mutationFn: async ({ data }: { data: ClientFE }) => {
      return setAdditionalClientAttributes(userId, data, token);
    },
  });

  const createClientUsersMutation = useMutation({
    onError(error) {
      console.error('Error creating client user:', error);
    },
    mutationFn: async ({ data }: { data: UserApi }) => {
      data.user_id = userId;
      return addClientUser(data, token);
    },
  });

  const updateClientUsersMutation = useMutation({
    onError(error) {
      console.error('Error updating client user:', error);
    },
    mutationFn: async ({
      data,
      username,
    }: {
      data: UserApi;
      username: string;
    }) => {
      return updateClientUser(userId, username, data, token);
    },
  });

  const deleteClientUsersMutation = useMutation({
    onError(error) {
      console.error('Error deleting client user:', error);
    },
    mutationFn: async ({ username }: { username: string | undefined }) => {
      return deleteClientUser(userId, token, username);
    },
  });

  const getClientUsersList = useQuery({
    queryKey: ['get_user_list', userId],
    queryFn: async () => {
      if (!userId) {
        throw new Error('userId is required');
      }
      return await getClientUsers(userId, token);
    },
    retry,
    enabled: !!userId && !!token,
  });

  return {
    setCognitoProfile,
    getAdditionalClientAttrs,
    createOrUpdateClientAttrsMutation,
    createClientUsersMutation,
    updateClientUsersMutation,
    deleteClientUsersMutation,
    getClientUsersList,
  };
};
