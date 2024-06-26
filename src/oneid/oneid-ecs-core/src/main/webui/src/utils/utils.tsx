import { ROUTE_LOGIN } from './constants';

export const redirectToLogin = () => {
  window.location.assign(`${ROUTE_LOGIN}?${forwardSearchParams()}`);
};

export const forwardSearchParams = (idp?: string) => {
  const searchParams = new URLSearchParams(window.location.search);
  if (idp) {
    searchParams.set('idp', idp);
  }
  const params = encodeURIComponent(searchParams.toString());
  return decodeURIComponent(params);
};
