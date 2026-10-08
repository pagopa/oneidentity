import { describe, it, expect, vi, Mock } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useLoginData } from './useLoginData';
import { ENV } from '../utils/env';
import { fetchBannerContent, getIdpList, getClientData } from '../services/api';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// Mock API functions
vi.mock('../services/api', () => ({
  fetchBannerContent: vi.fn(),
  getIdpList: vi.fn(),
  getClientData: vi.fn(),
}));

// Mock ENV
vi.mock('../utils/env', () => ({
  ENV: {
    JSON_URL: {
      ALERT: 'mock-alert-url',
      IDP_LIST: 'mock-idp-list-url',
      CLIENT_BASE_URL: 'mock-client-base-url.org',
    },
  },
}));
vi.stubGlobal('location', { search: '?client_id=mock-client-id' });

describe('useLoginData', () => {
  const wrapper = ({ children }: { children: React.ReactNode }) => {
    const queryClient = new QueryClient();
    return (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal('location', { search: '?client_id=mock-client-id' });
  });

  it('fetches banner content successfully', async () => {
    const mockBannerContent = [
      { title: 'Test Banner', description: 'Test Description' },
    ];
    (fetchBannerContent as Mock).mockResolvedValue(mockBannerContent);

    const { result } = renderHook(useLoginData, { wrapper });

    await waitFor(() =>
      expect(result.current.bannerQuery.isSuccess).toBe(true)
    );
    expect(fetchBannerContent).toHaveBeenCalledWith(ENV.JSON_URL.ALERT);
    expect(result.current.bannerQuery.data).toEqual(mockBannerContent);
  });

  it('fetches identity providers list successfully', async () => {
    const mockIdpList = {
      providers: [{ name: 'Test IDP', url: 'test-idp-url' }],
    };
    (getIdpList as Mock).mockResolvedValue(mockIdpList);

    const { result } = renderHook(useLoginData, { wrapper });

    await waitFor(() => expect(result.current.idpQuery.isSuccess).toBe(true));
    expect(getIdpList).toHaveBeenCalledWith(ENV.JSON_URL.IDP_LIST);
    expect(result.current.idpQuery.data).toEqual(mockIdpList);
  });

  it('fetches client data successfully', async () => {
    const mockClientData = {
      friendlyName: 'Test Client',
      logoUri: 'https://example.com/logo.png',
    };
    (getClientData as Mock).mockResolvedValue(mockClientData);

    const { result } = renderHook(useLoginData, { wrapper });

    await waitFor(() =>
      expect(result.current.clientQuery.isSuccess).toBe(true)
    );
    expect(getClientData).toHaveBeenCalledWith(
      ENV.JSON_URL.CLIENT_BASE_URL,
      'mock-client-id'
    );
    expect(result.current.clientQuery.data).toEqual(mockClientData);
  });

  it('isolates cached data when switching clients in the same app', async () => {
    const queryClient = new QueryClient();
    const sharedWrapper = ({ children }: { children: React.ReactNode }) => (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );
    (getClientData as Mock).mockImplementation((_baseUrl, clientID) =>
      Promise.resolve({
        clientID,
        callbackURI: [`https://${clientID}.example/callback`],
      })
    );
    vi.stubGlobal('location', { search: '?client_id=client-a' });
    const { result, rerender } = renderHook(useLoginData, {
      wrapper: sharedWrapper,
    });
    await waitFor(() =>
      expect(result.current.clientQuery.data?.clientID).toBe('client-a')
    );

    vi.stubGlobal('location', { search: '?client_id=client-b' });
    rerender();
    await waitFor(() =>
      expect(result.current.clientQuery.data?.clientID).toBe('client-b')
    );

    expect(queryClient.getQueryData(['clientData', 'client-a'])).toEqual({
      clientID: 'client-a',
      callbackURI: ['https://client-a.example/callback'],
    });
    expect(getClientData).toHaveBeenCalledWith(
      ENV.JSON_URL.CLIENT_BASE_URL,
      'client-b'
    );

    vi.stubGlobal('location', { search: '' });
    rerender();
    expect(result.current.clientQuery.data).toBeUndefined();
    expect(getClientData).toHaveBeenCalledTimes(2);
  });

  it('handles errors correctly', async () => {
    (fetchBannerContent as Mock).mockRejectedValue(
      new Error('Banner content error')
    );
    (getIdpList as Mock).mockRejectedValue(new Error('IDP list error'));
    (getClientData as Mock).mockRejectedValue(new Error('Client data error'));

    const { result } = renderHook(useLoginData, { wrapper });

    await waitFor(() => expect(result.current.bannerQuery.isError).toBe(true), {
      timeout: 10000,
    });
    expect(result.current.bannerQuery.error).toEqual(
      new Error('Banner content error')
    );

    await waitFor(() => expect(result.current.idpQuery.isError).toBe(true), {
      timeout: 10000,
    });
    expect(result.current.idpQuery.error).toEqual(new Error('IDP list error'));

    await waitFor(() => expect(result.current.clientQuery.isError).toBe(true), {
      timeout: 10000,
    });
    expect(result.current.clientQuery.error).toEqual(
      new Error('Client data error')
    );
  });
});
