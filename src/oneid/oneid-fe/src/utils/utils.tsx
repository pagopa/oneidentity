import { ProductEntity } from '@pagopa/mui-italia';
import { Client } from '../services/api';
import { ROUTE_LOGIN } from './constants';
import { ERROR_CODE } from '../hooks/useLoginError';
import { storageRead, storageWrite } from '../services/storage';

export type OIDCParameters = {
  scope: string;
  client_id: string;
  state: string;
  nonce: string;
  redirect_uri: string;
};

function isValidOIDCParameters(
  params: OIDCParameters | unknown
): params is OIDCParameters {
  return (
    (params as OIDCParameters).client_id !== undefined &&
    (params as OIDCParameters).scope !== undefined &&
    (params as OIDCParameters).state !== undefined &&
    (params as OIDCParameters).nonce !== undefined &&
    (params as OIDCParameters).redirect_uri !== undefined
  );
}

export const redirectToLogin = () => {
  window.location.assign(ROUTE_LOGIN);
};

export const redirectToLoginWithParams = () => {
  const params = forwardSearchParams();
  const route = params ? `${ROUTE_LOGIN}?${params}` : ROUTE_LOGIN;
  window.location.assign(route);
};

export const redirectToLoginToRetry = () => {
  const params = new URLSearchParams();
  const storedParams = storageRead('oidc_parameters', 'object', false);

  if (storedParams && isValidOIDCParameters(storedParams)) {
    Object.entries(storedParams).forEach(([key, value]) => {
      params.set(key, value as string);
    });
    console.log('redirectToLoginToRetry', params, storedParams);
    return `${ROUTE_LOGIN}?${params}`;
  } else {
    return null;
  }
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

export const writeParamsToSessionStorage = () => {
  const searchParams = new URLSearchParams(window.location.search);
  const params = Object.fromEntries(searchParams.entries());
  storageWrite('oidc_parameters', JSON.stringify(params), 'string', false);
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
