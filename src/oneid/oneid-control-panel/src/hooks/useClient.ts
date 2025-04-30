import { useQuery } from '@tanstack/react-query';
import { useAuth } from 'react-oidc-context';
import { setClientToUser } from '../api/client';

const staleTime = 5 * 60 * 1000;
const retry = 2;

export const useClient = (clientId?: string) => {
  const { user } = useAuth();
  const token = user?.id_token;
  if (!token) {
    throw new Error('No token available');
  }
  const userId = user?.profile.sub;

  const setCognitoProfile = useQuery<string, Error>({
    queryKey: ['client', clientId, userId],
    queryFn: () => setClientToUser(clientId, userId, token),
    enabled: !!token && !!clientId && !!userId,
    staleTime,
    retry,
    throwOnError: false, //be careful with this option, it can cause unexpected behavior
  });

  return {
    setCognitoProfile,
  };
};
