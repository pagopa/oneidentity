import { ENV } from './env';

export const ROUTE_LOGIN = '/login';
export const ROUTE_LOGIN_SUCCESS = '/login/success';
export const ROUTE_LOGIN_ERROR = '/login/error';
export const ROUTE_LOGOUT = '/logout';
export const ROUTE_COURTESY_SWITCHTOIO = '/courtesy/switch-to-io';
export const ROUTE_COURTESY_SERVICE_KO = '/courtesy/service-ko';

export const IDP_PLACEHOLDER_IMG =
  ENV.URL_FE.ASSETS +
  '/idps/aHR0cHM6Ly92YWxpZGF0b3IuZGV2Lm9uZWlkLnBhZ29wYS5pdC9kZW1v.png';

export const PRODUCTS_URL = ENV.URL_FE.ASSETS + '/products.json';
