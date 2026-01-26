import { AuthContextProps } from 'react-oidc-context';

let authContext: AuthContextProps | null = null;

export const setAuthInstance = (instance: AuthContextProps) => {
  authContext = instance;
};

export const getAccessToken = () => {
  return authContext?.user?.id_token || null;
};
