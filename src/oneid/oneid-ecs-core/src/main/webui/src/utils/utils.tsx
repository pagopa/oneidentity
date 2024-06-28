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
