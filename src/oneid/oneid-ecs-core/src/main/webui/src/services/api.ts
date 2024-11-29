import { ENV } from '../utils/env';
import { IdentityProvider } from '../utils/IDPS';

export type BannerContent = {
  enable: boolean;
  severity: 'warning' | 'error' | 'info' | 'success';
  description: string;
};

export type Client = {
  clientID: string;
  friendlyName: string;
  logoUri: string;
  policyUri: string;
  tosUri: string;
};

export const getIdpList = async (idpListUrl: string) => {
  try {
    const response = await fetch(idpListUrl);
    const res: Array<IdentityProvider> = await response.json();
    const assetsIDPUrl = ENV.URL_FE.ASSETS + '/idps';
    const rawIDPS = res
      .map((i) => ({
        ...i,
        imageUrl: `${assetsIDPUrl}/${btoa(i.entityID)}.png`,
      }))
      .sort(() => 0.5 - Math.random());
    const idps: {
      identityProviders: Array<IdentityProvider>;
      richiediSpid: string;
    } = {
      identityProviders: rawIDPS,
      richiediSpid: 'https://www.spid.gov.it/cos-e-spid/come-attivare-spid/',
    };
    return { idps };
  } catch (error) {
    console.error(error);
    return { idps: undefined };
  }
};

export const getClientData = async (clientBaseListUrl: string) => {
  try {
    const query = new URLSearchParams(window.location.search);
    const clientID = query.get('client_id');

    if (clientID && clientID.match(/^[A-Za-z0-9_-]{43}$/)) {
      const clientListUrl = `${clientBaseListUrl}/${clientID}`;
      const response = await fetch(clientListUrl);
      const res: Client = await response.json();
      return { clientData: res };
    } else {
      console.warn('no client_id supplied, or not valid 32bit Base64Url');
      return { clientData: undefined };
    }
  } catch (error) {
    console.error(error);
    return { clientData: undefined };
  }
};

export const fetchBannerContent = async (
  loginBannerUrl: string
): Promise<Array<BannerContent>> => {
  try {
    const response = await fetch(loginBannerUrl);
    const data = await response.json();
    return Object.values(data) as Array<BannerContent>;
  } catch (error) {
    console.error('Failed to fetch banner content:', error);
    return [];
  }
};
