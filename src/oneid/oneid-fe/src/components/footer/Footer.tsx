import {
  Footer as MuiItaliaFooter,
  FooterLinksType,
  PreLoginFooterLinksType,
} from '@pagopa/mui-italia/dist/components/Footer/Footer';
import { Trans, useTranslation } from 'react-i18next';
import { useEffect } from 'react';
import { LangCode } from '@pagopa/mui-italia';
import { ENV } from '../../utils/env';
import { LANGUAGES, pagoPALink } from './FooterConfig';
import i18n from '../../locale';
import { useLoginData } from '../../hooks/useLoginData';

type FooterProps = {
  productsJsonUrl?: string;
  onExit?: (exitAction: () => void) => void;
};
type Languages = typeof LANGUAGES;

declare const window: Window &
  typeof globalThis & {
    OneTrust: {
      ToggleInfoDisplay: () => void;
    };
  };

export default function Footer({
  productsJsonUrl,
  onExit = (exitAction) => exitAction(),
}: FooterProps) {
  const { clientQuery } = useLoginData();

  const { t } = useTranslation();

  const currentLangByUrl = new URLSearchParams(window.location.search).get(
    'lang'
  ) as LangCode;
  const lang = (
    currentLangByUrl ? currentLangByUrl : i18n.language
  ) as LangCode;

  useEffect(() => {
    if (lang) {
      void i18n.changeLanguage(lang);
    }
  }, [lang]);

  const preLoginLinks: PreLoginFooterLinksType = {
    // First column
    aboutUs: {
      title: undefined,
      links: [
        {
          label: t('common.footer.preLoginLinks.aboutUs.links.aboutUs'),
          href: ENV.FOOTER.LINK.ABOUTUS,
          ariaLabel: 'Vai al link: Chi siamo',
          linkType: 'internal',
        },
        {
          label: t('common.footer.preLoginLinks.aboutUs.links.media'),
          href: ENV.FOOTER.LINK.MEDIA,
          ariaLabel: 'Vai al link: Media',
          linkType: 'internal',
        },
        {
          label: t('common.footer.preLoginLinks.aboutUs.links.workwithud'),
          href: ENV.FOOTER.LINK.WORKWITHUS,
          ariaLabel: 'Vai al link: Lavora con noi',
          linkType: 'internal',
        },
      ],
    },
    // Third column
    resources: {
      title: t('common.footer.preLoginLinks.resources.title'),
      links: [
        {
          label: t('common.footer.preLoginLinks.resources.links.privacyPolicy'),
          href:
            clientQuery.data?.policyUri || ENV.URL_FOOTER.PRIVACY_DISCLAIMER,
          ariaLabel: 'Vai al link: Informativa Privacy',
          linkType: 'internal',
        },
        {
          label: t(
            'common.footer.preLoginLinks.resources.links.certifications'
          ),
          href: ENV.FOOTER.LINK.CERTIFICATIONS,
          ariaLabel: 'Vai al link: Certificazioni',
          linkType: 'internal',
        },
        {
          label: t(
            'common.footer.preLoginLinks.resources.links.informationsecurity'
          ),
          href: ENV.FOOTER.LINK.INFORMATIONSECURITY,
          ariaLabel: 'Vai al link: Sicurezza delle informazioni',
          linkType: 'internal',
        },
        {
          label: t(
            'common.footer.preLoginLinks.resources.links.protectionofpersonaldata'
          ),
          href: ENV.FOOTER.LINK.PROTECTIONOFPERSONALDATA,
          ariaLabel: 'Vai al link: Diritto alla protezione dei dati personali',
          linkType: 'internal',
        },
        {
          label: t('common.footer.preLoginLinks.resources.links.cookies'),
          // onClick: () => window.OneTrust.ToggleInfoDisplay(),
          href: clientQuery.data?.cookieUri || ENV.FOOTER.LINK.COOKIE,
          ariaLabel: 'Vai al link: Preferenze Cookie',
          linkType: 'internal',
        },
        {
          label: t(
            'common.footer.preLoginLinks.resources.links.termsandconditions'
          ),
          href: clientQuery.data?.tosUri || ENV.URL_FOOTER.TERMS_AND_CONDITIONS,
          ariaLabel: 'Vai al link: Termini e Condizioni',
          linkType: 'internal',
        },
        {
          label: t(
            'common.footer.preLoginLinks.resources.links.transparentcompany'
          ),
          href: ENV.FOOTER.LINK.TRANSPARENTCOMPANY,
          ariaLabel: 'Vai al link: Società trasparente',
          linkType: 'internal',
        },
        {
          label: t(
            'common.footer.preLoginLinks.resources.links.disclosurePolicy'
          ),
          href: ENV.FOOTER.LINK.DISCLOSUREPOLICY,
          ariaLabel: 'Vai al link: Responsible Disclosure Policy',
          linkType: 'internal',
        },
        {
          label: t('common.footer.preLoginLinks.resources.links.model231'),
          href: ENV.FOOTER.LINK.MODEL231,
          ariaLabel: 'Vai al link: Modello 231',
          linkType: 'internal',
        },
      ],
    },
    // Fourth column
    followUs: {
      title: t('common.footer.preLoginLinks.followUs.title'),
      socialLinks: [
        {
          icon: 'linkedin',
          title: 'LinkedIn',
          href: ENV.FOOTER.LINK.LINKEDIN,
          ariaLabel: 'Link: vai al sito LinkedIn di PagoPA S.p.A.',
        },
        {
          title: 'Twitter',
          icon: 'twitter',
          href: ENV.FOOTER.LINK.TWITTER,
          ariaLabel: 'Link: vai al sito Twitter di PagoPA S.p.A.',
        },
        {
          icon: 'instagram',
          title: 'Instagram',
          href: ENV.FOOTER.LINK.INSTAGRAM,
          ariaLabel: 'Link: vai al sito Instagram di PagoPA S.p.A.',
        },
        {
          icon: 'medium',
          title: 'Medium',
          href: ENV.FOOTER.LINK.MEDIUM,
          ariaLabel: 'Link: vai al sito Medium di PagoPA S.p.A.',
        },
      ],
      links: [
        {
          label: t('common.footer.preLoginLinks.accessibility'),
          href: ENV.FOOTER.LINK.ACCESSIBILITY,
          ariaLabel: 'Vai al link: Accessibilità',
          linkType: 'internal',
        },
      ],
    },
  };
  const postLoginLinks: Array<FooterLinksType> = [
    {
      label: t('common.footer.postLoginLinks.privacyPolicy'),
      href: ENV.URL_FOOTER.PRIVACY_DISCLAIMER,
      ariaLabel: 'Vai al link: Informativa Privacy',
      linkType: 'internal',
    },
    {
      label: t('common.footer.postLoginLinks.protectionofpersonaldata'),
      href: ENV.FOOTER.LINK.PROTECTIONOFPERSONALDATA,
      ariaLabel: 'Vai al link: Diritto alla protezione dei dati personali',
      linkType: 'internal',
    },
    {
      label: t('common.footer.postLoginLinks.termsandconditions'),
      href: ENV.URL_FOOTER.TERMS_AND_CONDITIONS,
      ariaLabel: 'Vai al link: Termini e condizioni',
      linkType: 'internal',
    },
    {
      label: t('common.footer.postLoginLinks.accessibility'),
      href: ENV.FOOTER.LINK.ACCESSIBILITY,
      ariaLabel: 'Vai al link: Accessibilità',
      linkType: 'internal',
    },
  ];
  const companyLegalInfo = (
    <Trans i18nKey="common.footer.legalInfoText">
      <strong>PagoPA S.p.A.</strong> - Società per azioni con socio unico -
      Capitale sociale di euro 1,000,000 interamente versato - Sede legale in
      Roma, Piazza Colonna 370, <br />
      CAP 00187 - N. di iscrizione a Registro Imprese di Roma, CF e P.IVA
      15376371009
    </Trans>
  );

  return (
    <MuiItaliaFooter
      companyLink={pagoPALink}
      postLoginLinks={postLoginLinks}
      preLoginLinks={preLoginLinks}
      legalInfo={companyLegalInfo}
      loggedUser={false}
      onExit={onExit}
      languages={LANGUAGES as Languages}
      onLanguageChanged={async (language: LangCode) => {
        await i18n.changeLanguage(language);
      }}
      currentLangCode={lang}
      productsJsonUrl={productsJsonUrl}
    />
  );
}
