import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { getIdpList, getClientData, fetchBannerContent } from './api';
import { ENV } from '../utils/env';

const MOCK_IDP_LIST_URL = 'https://example.com/idps';
const MOCK_CLIENT_BASE_LIST_URL = 'https://example.com/clients';
const MOCK_LOGIN_BANNER_URL = 'https://example.com/banner';
const SPID_RICHIEDI_URL =
  'https://www.spid.gov.it/cos-e-spid/come-attivare-spid/';
const INVALID_CLIENT_ID_WARNING =
  'no client_id supplied, or not valid 32bit Base64Url';

const mockFetch = vi.fn();

vi.stubGlobal('fetch', mockFetch);

describe('API Functions', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('getIdpList', () => {
    it('should return a sorted list of identity providers with image URLs', async () => {
      const mockIdps = [{ entityID: 'test-idp' }];
      const expectedImageUrl = `${ENV.URL_FE.ASSETS}/idps/${btoa('test-idp')}.png`;

      mockFetch.mockResolvedValueOnce({
        json: vi.fn().mockResolvedValueOnce(mockIdps),
      });

      const result = await getIdpList(MOCK_IDP_LIST_URL);

      expect(mockFetch).toHaveBeenCalledWith(MOCK_IDP_LIST_URL);
      expect(result.idps?.identityProviders).toEqual([
        { ...mockIdps[0], imageUrl: expectedImageUrl },
      ]);
      expect(result.idps?.richiediSpid).toBe(SPID_RICHIEDI_URL);
    });

    it('should return undefined idps on fetch error', async () => {
      mockFetch.mockRejectedValueOnce(new Error(INVALID_CLIENT_ID_WARNING));

      const result = await getIdpList(MOCK_IDP_LIST_URL);

      expect(mockFetch).toHaveBeenCalledWith(MOCK_IDP_LIST_URL);
      expect(result.idps).toBeUndefined();
    });
  });

  describe('getClientData', () => {
    beforeEach(() => {
      vi.stubGlobal('window', {
        location: { search: '?client_id=test-client-id' },
      });
    });

    afterEach(() => {
      vi.restoreAllMocks();
    });

    it.skip('should return client data when client ID is valid', async () => {
      const mockClientData = {
        clientID: 'test-client-id',
        friendlyName: 'Test Client',
      };

      mockFetch.mockResolvedValueOnce({
        json: vi.fn().mockResolvedValueOnce(mockClientData),
      });

      const result = await getClientData(MOCK_CLIENT_BASE_LIST_URL);

      expect(mockFetch).toHaveBeenCalledWith(
        `${MOCK_CLIENT_BASE_LIST_URL}/test-client-id`
      );
      expect(result.clientData).toEqual(mockClientData);
    });

    it('should return undefined client data for invalid client ID', async () => {
      vi.stubGlobal('window', {
        location: { search: '?client_id=invalid-id' },
      });

      const result = await getClientData(MOCK_CLIENT_BASE_LIST_URL);

      expect(result.clientData).toBeUndefined();
    });

    it('should return undefined client data on fetch error', async () => {
      mockFetch.mockRejectedValueOnce(new Error(INVALID_CLIENT_ID_WARNING));

      const result = await getClientData(MOCK_CLIENT_BASE_LIST_URL);

      expect(result.clientData).toBeUndefined();
    });
  });

  describe('fetchBannerContent', () => {
    it('should return an array of banner content', async () => {
      const mockBannerContent = {
        banner1: { enable: true, severity: 'info', description: 'Test Banner' },
        banner2: {
          enable: false,
          severity: 'error',
          description: 'Error Banner',
        },
      };

      mockFetch.mockResolvedValueOnce({
        json: vi.fn().mockResolvedValueOnce(mockBannerContent),
      });

      const result = await fetchBannerContent(MOCK_LOGIN_BANNER_URL);

      expect(mockFetch).toHaveBeenCalledWith(MOCK_LOGIN_BANNER_URL);
      expect(result).toEqual(Object.values(mockBannerContent));
    });

    it('should return an empty array on fetch error', async () => {
      mockFetch.mockRejectedValueOnce(new Error(INVALID_CLIENT_ID_WARNING));

      const result = await fetchBannerContent(MOCK_LOGIN_BANNER_URL);

      expect(mockFetch).toHaveBeenCalledWith(MOCK_LOGIN_BANNER_URL);
      expect(result).toEqual([]);
    });
  });
});
