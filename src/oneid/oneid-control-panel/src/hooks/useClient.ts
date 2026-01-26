import { IdpUser } from './../types/api';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useAuth } from 'react-oidc-context';
import {
  addClientUser,
  updateClientUser,
  deleteClientUser,
  getClientUsers,
} from '../api/client';

export const USER_LIST_QKEY = 'get_user_list';

const retry = 2;

export const useClient = () => {
  const { user } = useAuth();
  const userId = user?.profile.sub;

  const createIdpUserMutation = useMutation({
    onError(error) {
      console.error('Error creating idp user:', error);
    },
    mutationFn: async ({ data }: { data: IdpUser }) => {
      return addClientUser(data);
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
      return updateClientUser(username, data);
    },
  });

  const deleteClientUsersMutation = useMutation({
    onError(error) {
      console.error('Error deleting client user:', error);
    },
    mutationFn: async ({ username }: { username: string | undefined }) => {
      return deleteClientUser(username);
    },
  });

  const getClientUsersList = useQuery({
    queryKey: [USER_LIST_QKEY, userId],
    queryFn: async () => {
      return await getClientUsers();
    },
    retry,
    enabled: !!userId,
  });

  return {
    createClientUsersMutation: createIdpUserMutation,
    updateClientUsersMutation: updateIdpUsersMutation,
    deleteClientUsersMutation,
    getClientUsersList,
  };
};
