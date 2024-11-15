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

  ENABLED_SPID_TEMPORARY_SELECT: import.meta.env.VITE_LOGIN_SPID_ENABLED_TEMPORARY_SELECT === 'true',

  ASSISTANCE: {
    ENABLE: import.meta.env.VITE_ENABLE_ASSISTANCE ,
    EMAIL: import.meta.env.VITE_PAGOPA_HELP_EMAIL as string,
  },

  JSON_URL: {
    ALERT: import.meta.env.VITE_LOGIN_ALERT_BANNER as string,
    IDP_LIST: import.meta.env.VITE_LOGIN_IDP_LIST as string,
    CLIENT_BASE_URL: import.meta.env.VITE_LOGIN_CLIENT_BASE_URL as string,
  },

  URL_FE: {
    LOGIN: PUBLIC_URL + '/login',
    LOGOUT: PUBLIC_URL + '/logout',
    ASSETS: import.meta.env.VITE_URL_CDN as string,
  },

  HEADER: {
    LINK: {
      ROOTLINK: import.meta.env.VITE_HEADER_LINK_ROOTLINK as string,
      PRODUCTURL: import.meta.env.VITE_HEADER_LINK_PRODUCTURL as string,
    },
  },

  URL_DOCUMENTATION: 'https://docs.pagopa.it/area-riservata/',

  URL_API: {
    LOGIN: import.meta.env.VITE_URL_API_LOGIN as string,
    AUTHORIZE: import.meta.env.VITE_URL_API_AUTHORIZE as string,
  },

  URL_FOOTER: {
    PRIVACY_DISCLAIMER: import.meta.env.VITE_URL_PRIVACY_DISCLAIMER as string,
    TERMS_AND_CONDITIONS: import.meta.env.VITE_URL_TERMS_AND_CONDITIONS as string,
  },

  SPID_TEST_ENV_ENABLED: import.meta.env.VITE_SPID_TEST_ENV_ENABLED === 'true',

  SPID_CIE_ENTITY_ID: import.meta.env.VITE_SPID_CIE_ENTITY_ID as string,

  ANALYTCS: {
    ENABLE: import.meta.env.VITE_ANALYTICS_ENABLE === 'true',
    MOCK: import.meta.env.VITE_ANALYTICS_MOCK === 'true',
    DEBUG: import.meta.env.VITE_ANALYTICS_DEBUG === 'true',
    TOKEN: import.meta.env.VITE_SERVICE_EXAMPLE_TOKEN as string,
    API_HOST: import.meta.env.VITE_SERVICE_EXAMPLE_API_HOST,
  },
};
