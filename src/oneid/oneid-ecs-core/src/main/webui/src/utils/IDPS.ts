import { ENV } from './env';
export type IdentityProvider = {
  identifier: string;
  entityId: string;
  name: string;
  imageUrl: string;
};

// this need to be fetched through be api
const IDPS: { identityProviders: Array<IdentityProvider>; richiediSpid: string } = {
  identityProviders: [
    {
      identifier: 'Aruba',
      entityId: 'arubaid',
      name: 'Aruba.it ID',
      imageUrl: 'https://assets.cdn.io.italia.it/spid/idps/spid-idp-arubaid.png',
    },
    {
      identifier: 'Poste',
      entityId: 'posteid',
      name: 'Poste ID',
      imageUrl: 'https://assets.cdn.io.italia.it/spid/idps/spid-idp-posteid.png',
    },
    {
      identifier: 'Infocert',
      entityId: 'infocertid',
      name: 'Infocert ID',
      imageUrl: 'https://assets.cdn.io.italia.it/spid/idps/spid-idp-infocertid.png',
    },
    {
      identifier: 'Register',
      entityId: 'spiditalia',
      name: 'SpidItalia',
      imageUrl: 'https://assets.cdn.io.italia.it/spid/idps/spid-idp-spiditalia.png',
    },
    {
      identifier: 'Sielte',
      entityId: 'sielteid',
      name: 'Sielte id',
      imageUrl: 'https://assets.cdn.io.italia.it/spid/idps/spid-idp-sielteid.png',
    },
    {
      identifier: 'Namirial',
      entityId: 'namirialid',
      name: 'Namirial ID',
      imageUrl: 'https://assets.cdn.io.italia.it/spid/idps/spid-idp-namirialid.png',
    },
    {
      identifier: 'Tim',
      entityId: 'timid',
      name: 'TIM id',
      imageUrl: 'https://assets.cdn.io.italia.it/spid/idps/spid-idp-timid.png',
    },
    {
      identifier: 'Lepida',
      entityId: 'lepidaid',
      name: 'Lepida',
      imageUrl: 'https://assets.cdn.io.italia.it/spid/idps/spid-idp-lepidaid.png',
    },
    {
      identifier: 'TeamSystem',
      entityId: 'teamsystemid',
      name: 'TeamSystem',
      imageUrl: 'https://assets.cdn.io.italia.it/spid/idps/spid-idp-teamsystemid.png',
    },
    {
      identifier: 'EtnaHitech',
      entityId: 'ehtid',
      name: 'Etna Hitech S.C.p.A.',
      imageUrl: 'https://assets.cdn.io.italia.it/spid/idps/spid-idp-etnaid.png',
    },
    {
      identifier: 'InfoCamere',
      entityId: 'infocamereid',
      name: 'InfoCamere S.C.p.A.',
      imageUrl: 'https://assets.cdn.io.italia.it/spid/idps/spid-idp-infocamereid.png',
    },
    {
      identifier: 'Intesi Group SPID',
      entityId: 'intesiid',
      name: 'Intesi Group S.p.A',
      imageUrl: 'https://assets.cdn.io.italia.it/spid/idps/spid-idp-intesigroupspid.png',
    },
  ].sort(() => 0.5 - Math.random()),
  richiediSpid: 'https://www.spid.gov.it/cos-e-spid/come-attivare-spid/',
};
console.log("spid", ENV.SPID_TEST_ENV_ENABLED);

if (ENV.SPID_TEST_ENV_ENABLED) {

  IDPS.identityProviders.push({
    identifier: 'test',
    entityId: 'https://localhost:8443',
    name: 'test',
    imageUrl: 'https://upload.wikimedia.org/wikipedia/commons/1/11/Test-Logo.svg',
  }, 
  {
    identifier: 'validator',
    entityId: 'https://validator.dev.oneid.pagopa.it',
    name: 'validator',
    imageUrl: 'https://upload.wikimedia.org/wikipedia/commons/d/df/Validator-Test.png',
  }, 
  {
    identifier: 'demo',
    entityId: 'https://demo.spid.gov.it',
    name: 'demo',
    imageUrl: 'https://upload.wikimedia.org/wikipedia/commons/1/1e/D.E.M.O._Logo_2006.svg',
  },
  {
    identifier: 'demo_pagopa',
    entityId: 'https://validator.dev.oneid.pagopa.it/demo',
    name: 'demo_pagopa',
    imageUrl: 'https://pagopa.portaleamministrazionetrasparente.it/moduli/output_media.php?file=enti_trasparenza/2197912210O__Ologo-pagopa-spa.png',
  });
}
export { IDPS };
