import { useQuery } from '@tanstack/react-query';
import { ENV } from '../utils/env';
import { IdentityProviders } from '../utils/IDPS';
import {
  type BannerContent,
  type Client,
  fetchBannerContent,
  getIdpList,
  getClientData,
} from '../services/api';

const staleTime = 5 * 60 * 1000;
const retry = 2;

export const useLoginData = () => {
  const bannerQuery = useQuery<Array<BannerContent>, Error>({
    queryKey: ['bannerContent'],
    queryFn: () => fetchBannerContent(ENV.JSON_URL.ALERT),
    staleTime,
    retry,
  });

  const idpQuery = useQuery<IdentityProviders, Error>({
    queryKey: ['idpList'],
    queryFn: () => getIdpList(ENV.JSON_URL.IDP_LIST),
    staleTime,
    retry,
  });

  const clientQuery = useQuery<Client, Error>({
    queryKey: ['clientData'],
    queryFn: () => getClientData(ENV.JSON_URL.CLIENT_BASE_URL),
    staleTime,
    enabled() {
      const q = new URLSearchParams(window.location.search);
      const clientID = q.get('client_id');
      return !!clientID;
    },
    retry,
    throwOnError: false, //be careful with this option, it can cause unexpected behavior
  });

  return { bannerQuery, idpQuery, clientQuery };
};
