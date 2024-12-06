/* eslint-disable sonarjs/no-duplicate-string */
/* eslint-disable functional/immutable-data */
import { describe, it, expect, vi, beforeEach, Mock } from 'vitest';
import { ENV } from '../utils/env';
import { IdentityProvider } from '../utils/IDPS';
import { getIdpList, getClientData, fetchBannerContent } from './api';

vi.stubGlobal('fetch', vi.fn());

describe('Utils functions', () => {
  beforeEach(() => {
    vi.restoreAllMocks(); // Reset mocks before each test
  });

  describe('getIdpList', () => {
    const mockIDPList: Array<Omit<IdentityProvider, 'imageUrl'>> = [
      { entityID: 'idp1', name: 'IDP 1', identifier: 'idp-identifier-1' },
      { entityID: 'idp2', name: 'IDP 2', identifier: 'idp-identifier-2' },
    ];

    it('returns a sorted and enhanced IDP list', async () => {
      (global.fetch as Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => mockIDPList,
      });

      const result = await getIdpList('https://example.com/idp-list');
      const assetsIDPUrl = ENV.URL_FE.ASSETS + '/idps';

      expect(result.identityProviders).toHaveLength(2);
      expect(result.identityProviders[0]).toHaveProperty(
        'imageUrl',
        `${assetsIDPUrl}/${btoa(mockIDPList[0].entityID)}.png`
      );
    });

    it('throws an error if the fetch fails', async () => {
      (global.fetch as Mock).mockResolvedValueOnce({
        ok: false,
        statusText: 'Not Found',
      });

      await expect(getIdpList('https://example.com/idp-list')).rejects.toThrow(
        'Failed to fetch IDP list: Not Found'
      );
    });
  });

  describe('getClientData', () => {
    const clientID = '0000000000000000000000000000000000000000000';

    const mockClientData = {
      clientID: clientID,
      friendlyName: 'Test Client',
      logoUri: 'https://example.com/logo.png',
      policyUri: 'https://example.com/policy',
      tosUri: 'https://example.com/tos',
    };

    beforeEach(() => {
      vi.stubGlobal('window', {
        location: { search: `?client_id=${clientID}` },
      });
    });

    afterEach(() => {
      vi.restoreAllMocks();
    });

    it('fetches client data successfully', async () => {
      (global.fetch as Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => mockClientData,
      });

      const result = await getClientData('https://example.com/clients');
      expect(result).toEqual(mockClientData);
    });

    it('throws an error if client_id is invalid or missing', async () => {
      vi.stubGlobal('window', {
        location: { search: `?client_id=` },
      });

      await expect(
        getClientData('https://example.com/clients')
      ).rejects.toThrow('Invalid or missing client_id');
    });

    it('throws an error if the fetch fails', async () => {
      (global.fetch as Mock).mockResolvedValueOnce({
        ok: false,
        statusText: 'Unauthorized',
      });

      await expect(
        getClientData('https://example.com/clients')
      ).rejects.toThrow('Failed to fetch client data: Unauthorized');
    });
  });

  describe('fetchBannerContent', () => {
    const mockBannerContent = {
      banner1: {
        enable: true,
        severity: 'info',
        description: 'This is a test banner',
      },
      banner2: {
        enable: false,
        severity: 'warning',
        description: 'This is another test banner',
      },
    };

    it('returns an array of banner content', async () => {
      (global.fetch as Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => mockBannerContent,
      });

      const result = await fetchBannerContent('https://example.com/banner');
      expect(result).toHaveLength(2);
      expect(result[0]).toHaveProperty('description', 'This is a test banner');
    });

    it('throws an error if the fetch fails', async () => {
      (global.fetch as Mock).mockResolvedValueOnce({
        ok: false,
        statusText: 'Forbidden',
      });

      await expect(
        fetchBannerContent('https://example.com/banner')
      ).rejects.toThrow('Failed to fetch banner content: Forbidden');
    });
  });
});
