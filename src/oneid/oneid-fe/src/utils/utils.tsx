import { ProductEntity } from '@pagopa/mui-italia';
import { Client } from '../services/api';
import { ROUTE_LOGIN } from './constants';
import { ERROR_CODE } from '../hooks/useLoginError';

export const redirectToLogin = () => {
  window.location.assign(ROUTE_LOGIN);
};

export const redirectToLoginWithParams = () => {
  const params = forwardSearchParams();
  const route = params ? `${ROUTE_LOGIN}?${params}` : ROUTE_LOGIN;
  window.location.assign(route);
};

export const redirectToClientWithError = (
  errorCode: ERROR_CODE,
  redirectUri: string,
  state: string
) => {
  // https://www.rfc-editor.org/rfc/rfc6749.html#section-4.1.2.1
  //  HTTP/1.1 302 Found
  //  Location: https://client.example.com/cb?error=access_denied&state=xyz

  const params = new URLSearchParams();
  params.set('error', 'access_denied');
  params.set('error_description', errorCode);
  params.set('state', state);
  const route = `${redirectUri}?${params.toString()}`;
  window.location.assign(route);
};

export const forwardSearchParams = (idp?: string) => {
  const searchParams = new URLSearchParams(window.location.search);
  if (idp) {
    searchParams.set('idp', idp);
  }
  const params = encodeURIComponent(searchParams.toString());
  return decodeURIComponent(params);
};

export const mapClientToProduct = (
  client: Client | undefined,
  logoUri: React.ReactNode
): ProductEntity | null => {
  if (client) {
    return {
      id: client.clientID,
      title: '', // passing an empty title to display only the icon
      icon: logoUri,
      productUrl: client.policyUri,
      linkType: 'external',
    };
  }
  return null;
};
