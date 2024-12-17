/// <reference types="react-scripts" />
declare namespace NodeJS {
  type ProcessEnv = {
    NODE_ENV: 'development' | 'uat' | 'production';
  };
}

type Window = {
  Stripe: never;
};
