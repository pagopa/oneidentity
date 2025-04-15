import { ENV } from './env';

export const BASE_ROUTE = ENV.PUBLIC_URL;

export const ROUTE_LOGIN = '/login'; // should be /login
export const ROUTE_LOGIN_SUCCESS = '/login/success';
export const ROUTE_LOGIN_ERROR = '/login/error';
export const ROUTE_LOGOUT = ENV.URL_FE.LOGOUT;

export const IDP_PLACEHOLDER_IMG =
  '/assets/idps/aHR0cHM6Ly92YWxpZGF0b3IuZGV2Lm9uZWlkLnBhZ29wYS5pdC9kZW1v.png';
