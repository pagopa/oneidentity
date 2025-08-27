import { AddIdpUser, IdpUser } from './../types/api';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useAuth } from 'react-oidc-context';
import {
  addClientUser,
  updateClientUser,
  deleteClientUser,
  getClientUsers,
} from '../api/client';

const retry = 2;

export const useClient = () => {
  const { user } = useAuth();
  const token = user?.id_token;
  const userId = user?.profile.sub;

  if (!token) {
    throw new Error('No token available');
  }

  const createIdpUserMutation = useMutation({
    onError(error) {
      console.error('Error creating idp user:', error);
    },
    mutationFn: async ({ data }: { data: AddIdpUser }) => {
      const newUser = data.user_id ? data : { ...data, user_id: userId };
      return addClientUser(newUser, token);
    },
  });

  const updateIdpUsersMutation = useMutation({
    onError(error) {
      console.error('Error updating client user:', error);
    },
    mutationFn: async ({
      data,
      username,
    }: {
      data: IdpUser;
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
    createClientUsersMutation: createIdpUserMutation,
    updateClientUsersMutation: updateIdpUsersMutation,
    deleteClientUsersMutation,
    getClientUsersList,
  };
};
