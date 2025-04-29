import { useQuery } from '@tanstack/react-query';
import { useAuth } from 'react-oidc-context';
import { setClientParam } from '../api/client';

const staleTime = 5 * 60 * 1000;
const retry = 2;

export const useClient = (clientId?: string) => {
  const { user } = useAuth();
  const token = user?.access_token;
  if (!token) {
    throw new Error('No token available');
  }

  const setCognitoProfile = useQuery<string, Error>({
    queryKey: ['client', clientId],
    queryFn: () => setClientParam(clientId, token),
    enabled: !!token && !!clientId,
    staleTime,
    retry,
    throwOnError: false, //be careful with this option, it can cause unexpected behavior
  });

  return {
    setCognitoProfile,
  };
};
