import { useMutation, useQuery } from '@tanstack/react-query';
import { useAuth } from 'react-oidc-context';
import { getAdditionalClientAttributes, setClientToUser } from '../api/client';
import { useParams } from 'react-router-dom';

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

  return {
    setCognitoProfile,
    getAdditionalClientAttrs,
  };
};
