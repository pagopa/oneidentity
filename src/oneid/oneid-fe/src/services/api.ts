import { LangCode } from '@pagopa/mui-italia';
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
  docUri: string;
  a11yUri: string;
  cookieUri: string;
  backButtonEnabled: boolean;
  localizedContentMap: Record<
    LangCode,
    Record<'title' | 'description', string>
  >;
};

export type IDPList = {
  identityProviders: Array<IdentityProvider>;
  richiediSpid: string;
};

export const getIdpList = async (idpListUrl: string) => {
  const response = await fetch(idpListUrl);
  if (!response.ok) {
    throw new Error(`Failed to fetch IDP list: ${response.statusText}`);
  }

  const res: Array<IdentityProvider> = await response.json();
  const assetsIDPUrl = ENV.URL_FE.ASSETS + '/idps';
  const rawIDPS = res
    .map((i) => ({
      ...i,
      imageUrl: `${assetsIDPUrl}/${btoa(i.entityID)}.png`,
    }))
    .sort(() => 0.5 - Math.random());

  const out: IDPList = {
    identityProviders: rawIDPS,
    richiediSpid: 'https://www.spid.gov.it/cos-e-spid/come-attivare-spid/',
  };

  return out;
};

export const getClientData = async (clientBaseListUrl: string) => {
  const query = new URLSearchParams(window.location.search);
  const clientID = query.get('client_id');

  if (!clientID || !clientID.match(/^[A-Za-z0-9_-]{43}$/)) {
    throw new Error('Invalid or missing client_id');
  }

  const clientListUrl = `${clientBaseListUrl}/${clientID}`;
  const response = await fetch(clientListUrl);
  if (!response.ok) {
    throw new Error(`Failed to fetch client data: ${response.statusText}`);
  }

  return await response.json();
};

export const fetchBannerContent = async (
  loginBannerUrl: string
): Promise<Array<BannerContent>> => {
  const response = await fetch(loginBannerUrl);
  if (!response.ok) {
    throw new Error(`Failed to fetch banner content: ${response.statusText}`);
  }

  const data = await response.json();
  return Object.values(data) as Array<BannerContent>;
};
