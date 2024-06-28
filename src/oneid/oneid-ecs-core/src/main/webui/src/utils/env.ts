import * as env from 'env-var';

const PUBLIC_URL: string = env.get('PUBLIC_URL').default('').asString();
const currentEnv: string = env.get('REACT_APP_ENV').required().asString();

export const ENV = {
  ENV: currentEnv,
  PUBLIC_URL,

  ENABLED_SPID_TEMPORARY_SELECT: env.get('REACT_APP_LOGIN_SPID_ENABLED_TEMPORARY_SELECT').required().asBool(),

  ASSISTANCE: {
    ENABLE: env.get('REACT_APP_ENABLE_ASSISTANCE').required().asBool(),
    EMAIL: env.get('REACT_APP_PAGOPA_HELP_EMAIL').required().asString(),
  },

  JSON_URL: {
    ALERT: env.get('REACT_APP_LOGIN_ALERT_BANNER').required().asString(),
  },

  URL_FE: {
    LOGIN: PUBLIC_URL + '/login',
    LOGOUT: PUBLIC_URL + '/logout',
  },

  HEADER: {
    LINK: {
      ROOTLINK: env.get('REACT_APP_HEADER_LINK_ROOTLINK').required().asString(),
      PRODUCTURL: env.get('REACT_APP_HEADER_LINK_PRODUCTURL').required().asString(),
    },
  },

  URL_DOCUMENTATION: ' https://docs.pagopa.it/area-riservata/',

  URL_API: {
    LOGIN: env.get('REACT_APP_URL_API_LOGIN').required().asString(),
    AUTHORIZE: env.get('REACT_APP_URL_API_AUTHORIZE').required().asString(),
  },

  URL_FOOTER: {
    PRIVACY_DISCLAIMER: env.get('REACT_APP_URL_PRIVACY_DISCLAIMER').required().asString(),
    TERMS_AND_CONDITIONS: env.get('REACT_APP_URL_TERMS_AND_CONDITIONS').required().asString(),
  },

  SPID_TEST_ENV_ENABLED: env.get('REACT_APP_SPID_TEST_ENV_ENABLED').required().asBool(),

  SPID_CIE_ENTITY_ID: env.get('REACT_APP_SPID_CIE_ENTITY_ID').required().asString(),

  ANALYTCS: {
    ENABLE: env.get('REACT_APP_ANALYTICS_ENABLE').default('false').asBool(),
    MOCK: env.get('REACT_APP_ANALYTICS_MOCK').default('false').asBool(),
    DEBUG: env.get('REACT_APP_ANALYTICS_DEBUG').default('false').asBool(),
    TOKEN: env.get('REACT_APP_SERVICE_EXAMPLE_TOKEN').required().asString(),
    API_HOST: env
      .get('REACT_APP_SERVICE_EXAMPLE_API_HOST')
      .default('https://examples.com')
      .asString(),
  },
};
