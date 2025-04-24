import { useQuery } from '@tanstack/react-query';
import { useAuth } from 'react-oidc-context';
import { setCognitoParam } from '../services/cognito.api';

const staleTime = 5 * 60 * 1000;
const retry = 2;

export const useCognito = (clientId?: string) => {
  const { user } = useAuth();
  const token = user?.access_token;
  if (!token) {
    throw new Error('No token available');
  }

  const setCognitoProfile = useQuery<string, Error>({
    queryKey: ['client', clientId],
    queryFn: () => setCognitoParam(clientId, token),
    enabled: !!token && !!clientId,
    staleTime,
    retry,
    throwOnError: false, //be careful with this option, it can cause unexpected behavior
  });

  return {
    setCognitoProfile,
  };
};
