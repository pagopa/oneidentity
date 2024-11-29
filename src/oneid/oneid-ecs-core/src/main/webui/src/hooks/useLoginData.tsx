import { useEffect, useState } from 'react';
import { ENV } from '../utils/env';
import { IdentityProviders } from '../utils/IDPS';
import {
  type BannerContent,
  type Client,
  fetchBannerContent,
  getIdpList,
  getClientData,
} from '../services/api';

export const useLoginData = () => {
  const [bannerContent, setBannerContent] = useState<Array<BannerContent>>();
  const [idpList, setIdpList] = useState<IdentityProviders>({
    identityProviders: [],
    richiediSpid: '',
  });
  const [clientData, setClientData] = useState<Client>();

  useEffect(() => {
    const bannerRequest = fetchBannerContent(ENV.JSON_URL.ALERT);
    const idpsRequest = getIdpList(ENV.JSON_URL.IDP_LIST);
    const clientDataRequest = getClientData(ENV.JSON_URL.CLIENT_BASE_URL);

    Promise.allSettled([idpsRequest, clientDataRequest, bannerRequest]).then(
      ([idpsResult, clientDataResult, bannerResult]) => {
        if (idpsResult.status === 'fulfilled' && idpsResult.value.idps) {
          setIdpList(idpsResult.value.idps);
        } else {
          console.error('Failed to fetch IDP list:', idpsResult.status);
        }

        if (
          clientDataResult.status === 'fulfilled' &&
          clientDataResult.value.clientData
        ) {
          setClientData(clientDataResult.value.clientData);
        } else {
          console.error(
            'Failed to fetch client data:',
            clientDataResult.status
          );
        }

        if (bannerResult.status === 'fulfilled' && bannerResult.value?.length) {
          setBannerContent(bannerResult.value);
        } else {
          console.error('Failed to fetch banner content:', bannerResult.status);
        }
      }
    );
  }, []);

  return { bannerContent, idpList, clientData };
};
