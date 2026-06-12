import { Trans, useTranslation } from 'react-i18next';
import { useEffect } from 'react';
import {
  Footer as MuiItaliaFooter,
  LangCode,
  PreLoginFooterLinksType,
} from '@pagopa/mui-italia';
import { ENV } from '../../utils/env';
import { getPreLoginFooterLinkDefinitions } from '../../utils/constants';
import { LANGUAGES, pagoPALink } from './FooterConfig';
import i18n from '../../locale';
import { useLoginData } from '../../hooks/useLoginData';

type FooterProps = {
  hidePreFooter?: boolean;
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
  hidePreFooter = false,
}: FooterProps) {
  const { clientQuery } = useLoginData();

  const { t } = useTranslation();

  const currentLangByUrl = new URLSearchParams(window.location.search).get(
    'lang'
  ) as LangCode;
  const lang = (
    currentLangByUrl ? encodeURIComponent(currentLangByUrl) : i18n.language
  ) as LangCode;
  const themeParam = encodeURIComponent(
    new URLSearchParams(window.location.search).get('theme') || 'default'
  );
  const localizedContent =
    clientQuery.data?.localizedContentMap?.[themeParam]?.[lang];
  const preLoginFooterLinkDefinitions = getPreLoginFooterLinkDefinitions({
    cookieHref: localizedContent?.cookieUri || ENV.FOOTER.LINK.COOKIE,
    accessibilityHref:
      clientQuery.data?.a11yUri || ENV.FOOTER.LINK.ACCESSIBILITY,
  });

  useEffect(() => {
    if (lang) {
      void i18n.changeLanguage(lang);
    }
  }, [lang]);

  const preLoginLinks: PreLoginFooterLinksType = {
    // First column
    aboutUs: {
      title: undefined,
      links: preLoginFooterLinkDefinitions.aboutUs.map((link) => ({
        ...link,
        label: t(link.translationKey),
      })),
    },
    // Third column
    resources: {
      title: t('common.footer.preLoginLinks.resources.title'),
      links: preLoginFooterLinkDefinitions.resources.map((link) => ({
        ...link,
        label: t(link.translationKey),
      })),
    },
    // Fourth column
    followUs: {
      title: t('common.footer.preLoginLinks.followUs.title'),
      socialLinks: [...preLoginFooterLinkDefinitions.followUsSocial],
      links: preLoginFooterLinkDefinitions.followUs.map((link) => ({
        ...link,
        label: t(link.translationKey),
      })),
    },
  };

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
      postLoginLinks={[]}
      preLoginLinks={preLoginLinks}
      legalInfo={companyLegalInfo}
      loggedUser={hidePreFooter}
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
