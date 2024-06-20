import { ROUTE_LOGIN } from './constants';

export const redirectToLogin = () => {
  window.location.assign(ROUTE_LOGIN);
};

export const forwardSearchParams = () => {
  const searchParams = new URLSearchParams(window.location.search);
  const params = encodeURIComponent(searchParams.toString());
  return decodeURIComponent(params);
};
