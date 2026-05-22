import type { PreLoginFooterSocialLink } from '@pagopa/mui-italia';
import { ENV } from './env';

export const ROUTE_LOGIN = '/login';
export const ROUTE_LOGIN_SUCCESS = '/login/success';
export const ROUTE_LOGIN_ERROR = '/login/error';
export const ROUTE_LOGOUT = '/logout';

export type FooterLinkDefinition = {
  translationKey: string;
  ariaLabel: string;
  href: string;
  linkType: 'internal';
};

export type FooterSocialLinkDefinition = PreLoginFooterSocialLink & {
  ariaLabel: NonNullable<PreLoginFooterSocialLink['ariaLabel']>;
  href: NonNullable<PreLoginFooterSocialLink['href']>;
};

type FooterLinkOptions = {
  cookieHref: string;
  accessibilityHref: string;
};

export const getPreLoginFooterLinkDefinitions = ({
  cookieHref,
  accessibilityHref,
}: FooterLinkOptions) => ({
  aboutUs: [
    {
      translationKey: 'common.footer.preLoginLinks.aboutUs.links.aboutUs',
      href: ENV.FOOTER.LINK.ABOUTUS,
      ariaLabel: 'Vai al link: Chi siamo',
      linkType: 'internal',
    },
    {
      translationKey: 'common.footer.preLoginLinks.aboutUs.links.pnrr',
      href: ENV.FOOTER.LINK.PNRR,
      ariaLabel: 'Vai al link: PNRR',
      linkType: 'internal',
    },
    {
      translationKey: 'common.footer.preLoginLinks.aboutUs.links.media',
      href: ENV.FOOTER.LINK.MEDIA,
      ariaLabel: 'Vai al link: Media',
      linkType: 'internal',
    },
    {
      translationKey: 'common.footer.preLoginLinks.aboutUs.links.workwithud',
      href: ENV.FOOTER.LINK.WORKWITHUS,
      ariaLabel: 'Vai al link: Lavora con noi',
      linkType: 'internal',
    },
  ] satisfies ReadonlyArray<FooterLinkDefinition>,
  resources: [
    {
      translationKey:
        'common.footer.preLoginLinks.resources.links.certifications',
      href: ENV.FOOTER.LINK.CERTIFICATIONS,
      ariaLabel: 'Vai al link: Certificazioni',
      linkType: 'internal',
    },
    {
      translationKey:
        'common.footer.preLoginLinks.resources.links.informationsecurity',
      href: ENV.FOOTER.LINK.INFORMATIONSECURITY,
      ariaLabel: 'Vai al link: Sicurezza delle informazioni',
      linkType: 'internal',
    },
    {
      translationKey:
        'common.footer.preLoginLinks.resources.links.protectionofpersonaldata',
      href: ENV.FOOTER.LINK.PROTECTIONOFPERSONALDATA,
      ariaLabel: 'Vai al link: Diritto alla protezione dei dati personali',
      linkType: 'internal',
    },
    {
      translationKey: 'common.footer.preLoginLinks.resources.links.cookies',
      href: cookieHref,
      ariaLabel: 'Vai al link: Preferenze Cookie',
      linkType: 'internal',
    },
    {
      translationKey:
        'common.footer.preLoginLinks.resources.links.transparentcompany',
      href: ENV.FOOTER.LINK.TRANSPARENTCOMPANY,
      ariaLabel: 'Vai al link: Società trasparente',
      linkType: 'internal',
    },
    {
      translationKey:
        'common.footer.preLoginLinks.resources.links.disclosurePolicy',
      href: ENV.FOOTER.LINK.DISCLOSUREPOLICY,
      ariaLabel: 'Vai al link: Responsible Disclosure Policy',
      linkType: 'internal',
    },
    {
      translationKey: 'common.footer.preLoginLinks.resources.links.model231',
      href: ENV.FOOTER.LINK.MODEL231,
      ariaLabel: 'Vai al link: Modello 231',
      linkType: 'internal',
    },
  ] satisfies ReadonlyArray<FooterLinkDefinition>,
  followUsSocial: [
    {
      icon: 'linkedin',
      title: 'LinkedIn',
      href: ENV.FOOTER.LINK.LINKEDIN,
      ariaLabel: 'Link: vai al sito LinkedIn di PagoPA S.p.A.',
    },
    {
      icon: 'instagram',
      title: 'Instagram',
      href: ENV.FOOTER.LINK.INSTAGRAM,
      ariaLabel: 'Link: vai al sito Instagram di PagoPA S.p.A.',
    },
    {
      icon: 'threads',
      title: 'Threads',
      href: ENV.FOOTER.LINK.THREADS,
      ariaLabel: 'Link: vai al sito Threads di PagoPA S.p.A.',
    },
    {
      icon: 'youtube',
      title: 'YouTube',
      href: ENV.FOOTER.LINK.YOUTUBE,
      ariaLabel: 'Link: vai al sito YouTube di PagoPA S.p.A.',
    },
  ] satisfies ReadonlyArray<FooterSocialLinkDefinition>,
  followUs: [
    {
      translationKey: 'common.footer.preLoginLinks.accessibility',
      href: accessibilityHref,
      ariaLabel: 'Vai al link: Accessibilità',
      linkType: 'internal',
    },
  ] satisfies ReadonlyArray<FooterLinkDefinition>,
});

export const IDP_PLACEHOLDER_IMG =
  ENV.URL_FE.ASSETS +
  '/idps/aHR0cHM6Ly92YWxpZGF0b3IuZGV2Lm9uZWlkLnBhZ29wYS5pdC9kZW1v.png';

export const PRODUCTS_URL = ENV.URL_FE.ASSETS + '/products.json';
