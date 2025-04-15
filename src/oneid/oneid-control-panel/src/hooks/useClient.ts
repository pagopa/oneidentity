import { useQuery, useMutation } from '@tanstack/react-query';
import { ClientData } from '../types/api';
import { useAuth } from '../contexts/AuthContext';

const TIMEOUT_DURATION = 10000; // 10 seconds

const withTimeout = <T>(promise: Promise<T>, timeoutMs: number): Promise<T> => {
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

export const useClient = (clientId: string) => {
  const { token } = useAuth();

  const { data, error, isLoading } = useQuery({
    queryKey: ['client', clientId],
    queryFn: () => {
      if (!token) throw new Error('No token available');
      return apiService.getClientData(clientId, token);
    },
    enabled: !!token && !!clientId,
    retry: false,
    staleTime: 0,
    gcTime: 0,
  });

  const {
    mutate,
    isPending: isSaving,
    error: saveError,
  } = useMutation({
    mutationFn: (
      updatedData: Partial<Omit<ClientData, 'client_id' | 'client_secret'>>
    ) => {
      if (!token) throw new Error('No token available');
      return apiService.createOrUpdateClient(updatedData, token, clientId);
    },
  });

  return {
    clientData: data,
    error,
    isLoading,
    updateClient: mutate,
    isSaving,
    saveError,
  };
};

export const useUpdateClient = () => {
  const { token } = useAuth();

  return useMutation({
    mutationFn: async ({
      data,
      clientId,
    }: {
      data: Partial<Omit<ClientData, 'client_id' | 'client_secret'>>;
      clientId?: string;
    }) => {
      if (!token) throw new Error('No token available');
      return withTimeout(
        apiService.createOrUpdateClient(data, token, clientId),
        TIMEOUT_DURATION
      );
    },
  });
};
