export const ENV = {
  CURRENT_ENV: import.meta.env.VITE_CURRENT_ENV as string,
  // Corresponds to the .env.[mode] file loaded
  // By default vite set mode to development when using vite dev
  // and set to production when using vite build.
  // Can be overriden using --mode option
  // see https://vite.dev/guide/env-and-mode#modes
  MODE: import.meta.env.MODE,

  // The following will disable the modal for selection of the SPID IDP
  // and will switch to SpidSelect page
  ENABLED_SPID_TEMPORARY_SELECT:
    import.meta.env.VITE_LOGIN_SPID_ENABLED_TEMPORARY_SELECT === 'true',

  FALLBACK_ASSISTANCE: {
    ENABLE: import.meta.env.VITE_ENABLE_FALLBACK_ASSISTANCE === 'true',
    EMAIL: import.meta.env.VITE_PAGOPA_FALLBACK_SUPPORT_EMAIL as string,
  },

  JSON_URL: {
    ALERT: import.meta.env.VITE_LOGIN_ALERT_BANNER as string,
    IDP_LIST: import.meta.env.VITE_LOGIN_IDP_LIST as string,
    CLIENT_BASE_URL: import.meta.env.VITE_LOGIN_CLIENT_BASE_URL as string,
  },

  URL_FE: {
    ASSETS: import.meta.env.VITE_CDN_URL as string,
  },

  HEADER: {
    LINK: {
      PAGOPALINK: import.meta.env.VITE_HEADER_LINK_PAGOPALINK as string,
    },
  },

  URL_DOCUMENTATION: 'https://docs.pagopa.it/home',

  URL_API: {
    LOGIN: import.meta.env.VITE_URL_API_LOGIN as string,
    AUTHORIZE: import.meta.env.VITE_URL_API_AUTHORIZE as string,
  },

  URL_FOOTER: {
    PRIVACY_DISCLAIMER: import.meta.env.VITE_URL_PRIVACY_DISCLAIMER as string,
    TERMS_AND_CONDITIONS: import.meta.env
      .VITE_URL_TERMS_AND_CONDITIONS as string,
  },

  FOOTER: {
    LINK: {
      ABOUTUS: import.meta.env.VITE_FOOTER_LINK_ABOUTUS as string,
      MEDIA: import.meta.env.VITE_FOOTER_LINK_MEDIA as string,
      WORKWITHUS: import.meta.env.VITE_FOOTER_LINK_WORKWITHUS as string,
      ACCESSIBILITY: import.meta.env.VITE_FOOTER_LINK_ACCESSIBILITY as string,
      COOKIE: import.meta.env.VITE_FOOTER_LINK_COOKIE as string,
      CERTIFICATIONS: import.meta.env.VITE_FOOTER_LINK_CERTIFICATIONS as string,
      INFORMATIONSECURITY: import.meta.env
        .VITE_FOOTER_LINK_INFORMATIONSECURITY as string,
      PROTECTIONOFPERSONALDATA: import.meta.env
        .VITE_FOOTER_LINK_PROTECTIONOFPERSONALDATA as string,
      TRANSPARENTCOMPANY: import.meta.env
        .VITE_FOOTER_LINK_TRANSPARENTCOMPANY as string,
      DISCLOSUREPOLICY: import.meta.env
        .VITE_FOOTER_LINK_DISCLOSUREPOLICY as string,
      MODEL231: import.meta.env.VITE_FOOTER_LINK_MODEL231 as string,
      LINKEDIN: import.meta.env.VITE_FOOTER_LINK_LINKEDIN as string,
      TWITTER: import.meta.env.VITE_FOOTER_LINK_TWITTER as string,
      INSTAGRAM: import.meta.env.VITE_FOOTER_LINK_INSTAGRAM as string,
      MEDIUM: import.meta.env.VITE_FOOTER_LINK_MEDIUM as string,
      PAGOPALINK: import.meta.env.VITE_FOOTER_LINK_PAGOPALINK as string,
    },
  },

  SPID_TEST_ENV_ENABLED: import.meta.env.VITE_SPID_TEST_ENV_ENABLED === 'true',

  CIE_ENTITY_ID: import.meta.env.VITE_CIE_ENTITY_ID as string,

  ANALYTICS: {
    ENABLE: import.meta.env.VITE_ANALYTICS_ENABLE === 'true',
    MOCK: import.meta.env.VITE_ANALYTICS_MOCK === 'true',
    DEBUG: import.meta.env.VITE_ANALYTICS_DEBUG === 'true',
    API_HOST: import.meta.env.VITE_SERVICE_EXAMPLE_API_HOST,
  },
};
