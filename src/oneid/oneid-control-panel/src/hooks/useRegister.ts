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

export const useRegister = () => {
  const { user } = useAuth();
  const token = user?.id_token;
  const userId = user?.profile.sub;

  if (!token) {
    throw new Error('No token available');
  }

  const clientQuery = useQuery<Client, Error>({
    queryKey: ['user-client', userId],
    queryFn: () => getClientData(userId, token),
    enabled: !!token && !!userId,
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
      data: Omit<Client, 'clientId' | 'clientSecret'>;
      clientId?: string;
    }) => {
      const dataWithUserId = { ...data, userId };
      return withTimeout(
        createOrUpdateClient(dataWithUserId, token, clientId),
        TIMEOUT_DURATION
      );
    },
  });

  return {
    clientQuery,
    createOrUpdateClientMutation,
  };
};
