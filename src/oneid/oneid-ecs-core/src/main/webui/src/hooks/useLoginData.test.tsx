import { Mock, vi } from 'vitest';
import { useLoginData } from './useLoginData';
import { fetchBannerContent, getIdpList, getClientData } from '../services/api';
import { ENV } from '../utils/env';
import { renderHook, waitFor } from '@testing-library/react';

vi.mock('../services/api', () => ({
  fetchBannerContent: vi.fn(),
  getIdpList: vi.fn(),
  getClientData: vi.fn(),
}));

describe('useLoginData', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should fetch and set bannerContent, idpList, and clientData on mount', async () => {
    const mockBanner = [
      { enable: true, severity: 'info', description: 'Test Banner' },
    ];
    const mockIdpList = {
      identityProviders: [{ entityID: 'test-idp' }],
      richiediSpid: 'https://example.com/spid',
    };
    const mockClientData = {
      clientID: 'test-client-id',
      friendlyName: 'Test Client',
      logoUri: 'https://example.com/logo.png',
      policyUri: 'https://example.com/policy',
      tosUri: 'https://example.com/tos',
    };

    // Mock API responses
    (fetchBannerContent as Mock).mockResolvedValueOnce(mockBanner);
    (getIdpList as Mock).mockResolvedValueOnce({ idps: mockIdpList });
    (getClientData as Mock).mockResolvedValueOnce({
      clientData: mockClientData,
    });

    // Render the hook
    const { result } = renderHook(useLoginData);

    await waitFor(() => {
      expect(fetchBannerContent).toHaveBeenCalledWith(ENV.JSON_URL.ALERT);
      expect(getIdpList).toHaveBeenCalledWith(ENV.JSON_URL.IDP_LIST);
      expect(getClientData).toHaveBeenCalledWith(ENV.JSON_URL.CLIENT_BASE_URL);
    });

    // Check if the state is updated correctly
    expect(result.current.bannerContent).toEqual(mockBanner);
    expect(result.current.idpList).toEqual(mockIdpList);
    expect(result.current.clientData).toEqual(mockClientData);
  });

  it('should not set state if API calls fail', async () => {
    // Mock failed API responses
    (fetchBannerContent as Mock).mockRejectedValueOnce(
      new Error('Failed to fetch banner')
    );
    (getIdpList as Mock).mockRejectedValueOnce(
      new Error('Failed to fetch IDP list')
    );
    (getClientData as Mock).mockRejectedValueOnce(
      new Error('Failed to fetch client data')
    );

    const { result } = renderHook(useLoginData);

    await waitFor(() => {
      // Check that the state has not been set
      expect(result.current.bannerContent).toBeUndefined();
      expect(result.current.idpList).toEqual({
        identityProviders: [],
        richiediSpid: '',
      });
      expect(result.current.clientData).toBeUndefined();
    });
  });

  it('should handle partial data successfully', async () => {
    const mockBanner = [
      { enable: true, severity: 'info', description: 'Test Banner' },
    ];
    const mockIdpList = {
      identityProviders: [{ entityID: 'test-idp' }],
      richiediSpid: 'https://example.com/spid',
    };

    // Mock API responses
    (fetchBannerContent as Mock).mockResolvedValueOnce(mockBanner);
    (getIdpList as Mock).mockResolvedValueOnce({ idps: mockIdpList });
    (getClientData as Mock).mockResolvedValueOnce({ clientData: undefined }); // No client data

    const { result } = renderHook(useLoginData);

    await waitFor(() => {
      // Verify that bannerContent and idpList are correctly set
      expect(result.current.bannerContent).toEqual(mockBanner);
      expect(result.current.idpList).toEqual(mockIdpList);
      expect(result.current.clientData).toBeUndefined(); // clientData should be undefined
    });
  });
});
