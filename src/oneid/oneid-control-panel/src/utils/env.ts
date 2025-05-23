const PUBLIC_URL: string = import.meta.env.VITE_PUBLIC_URL || '';

export const ENV = {
  CURRENT_ENV: import.meta.env.VITE_CURRENT_ENV as string,
  // Corresponds to the .env.[mode] file loaded
  // By default vite set mode to development when using vite dev
  // and set to production when using vite build.
  // Can be overriden using --mode option
  // see https://vite.dev/guide/env-and-mode#modes
  MODE: import.meta.env.MODE,
  PUBLIC_URL,

  URL_FE: {
    LOGIN: PUBLIC_URL + '/login',
    LOGOUT: PUBLIC_URL + '/logout',
    ASSETS: import.meta.env.VITE_URL_CDN as string,
  },

  URL_API: {
    LOGIN: import.meta.env.VITE_URL_API_LOGIN as string,
    REGISTER: import.meta.env.VITE_URL_API_REGISTER as string,
    CLIENT: {
      USER_ATTRIBUTES: import.meta.env
        .VITE_URL_API_CLIENT_USER_ATTRIBUTES as string,
    },
  },
  OIDC: {
    API: import.meta.env.VITE_COGNITO_API as string,
    TOKEN: (import.meta.env.VITE_COGNITO_API as string) + '/oauth2/token',
    DOMAIN: import.meta.env.VITE_COGNITO_DOMAIN as string,
    CLIENT_ID: import.meta.env.VITE_COGNITO_CLIENT_ID as string,
    REDIRECT_URI: import.meta.env.VITE_COGNITO_REDIRECT_URI as string,
    LOGOUT_URI: import.meta.env.VITE_COGNITO_LOGOUT_URI as string,
    RESPONSE_TYPE: import.meta.env.VITE_COGNITO_RESPONSE_TYPE as string,
    SCOPE: import.meta.env.VITE_COGNITO_SCOPE as string,
  },
};
