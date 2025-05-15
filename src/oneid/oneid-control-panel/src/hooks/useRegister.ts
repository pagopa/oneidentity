import { useQuery, useMutation } from '@tanstack/react-query';
import { Client } from '../types/api';
import { getClientData, createOrUpdateClient } from '../api/register';
import { useAuth } from 'react-oidc-context';

const TIMEOUT_DURATION = 10000; // 10 seconds
const staleTime = 5 * 60 * 1000;
const retry = 2;

const withTimeout = <T extends object>(
  promise: Promise<T>,
  timeoutMs: number
): Promise<T> => {
  return Promise.race([
    promise,
    new Promise<T>((_, reject) =>
      setTimeout(
        () =>
          reject(
            new Error(
              'Request timed out. Please check your network connection.'
            )
          ),
        timeoutMs
      )
    ),
  ]);
};

export const useRegister = (clientId?: string) => {
  const { user } = useAuth();
  const token = user?.id_token;
  if (!token) {
    throw new Error('No token available');
  }

  const clientQuery = useQuery<Client, Error>({
    queryKey: ['client', clientId],
    queryFn: () => getClientData(clientId, token),
    enabled: !!token && !!clientId,
    staleTime,
    retry,
    throwOnError: false, //be careful with this option, it can cause unexpected behavior
  });

  const createOrUpdateClientMutation = useMutation({
    onError(error) {
      console.error('Error creating or updating client:', error);
    },
    mutationFn: async ({
      data,
      clientId,
    }: {
      data: Omit<Client, 'client_id' | 'client_secret'>;
      clientId?: string;
    }) => {
      return withTimeout(
        createOrUpdateClient(data, token, clientId),
        TIMEOUT_DURATION
      );
    },
  });

  return {
    clientQuery,
    createOrUpdateClientMutation,
  };
};
