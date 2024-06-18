import { ROUTE_LOGIN } from './constants';

export const redirectToLogin = () => {
  window.location.assign(ROUTE_LOGIN);
};

export const isPnpg =
window.location.hostname?.startsWith('pnpg') || window.location.hostname?.startsWith('imprese');