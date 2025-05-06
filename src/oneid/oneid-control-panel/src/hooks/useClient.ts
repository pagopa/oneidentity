import { useMutation } from '@tanstack/react-query';
import { useAuth } from 'react-oidc-context';
import { setClientToUser } from '../api/client';

const retry = 2;

export const useClient = () => {
  const { user } = useAuth();
  const token = user?.id_token;
  const userId = user?.profile.sub;
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

  return {
    setCognitoProfile,
  };
};
