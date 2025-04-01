import { ProductEntity } from '@pagopa/mui-italia';
import { Client } from '../services/api';
import { ROUTE_LOGIN } from './constants';

export const redirectToLogin = () => {
  const params = forwardSearchParams();
  const route = params ? `${ROUTE_LOGIN}?${params}` : ROUTE_LOGIN;
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

export const isUrlInSameOrigin = (urlString: string): boolean => {
  try {
    const url = new URL(urlString, window.location.origin);
    if (url.origin === window.location.origin) {
      return true;
    } else {
      console.error('Invalid redirect URI');
      return false;
    }
  } catch (e) {
    console.error('Invalid URL format', e);
    return false;
  }
};
