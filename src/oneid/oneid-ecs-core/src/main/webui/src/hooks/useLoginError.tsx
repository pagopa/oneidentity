import { t } from 'i18next';
import { Trans } from 'react-i18next';

export type ErrorData = {
  title: string | JSX.Element;
  description: string | JSX.Element;
  haveRetryButton: boolean;
};

export const useLoginError = () => {
  const handleErrorCode = (errorCode: string) => {
    switch (errorCode) {
      case '19':
        return {
          title: (
            <Trans i18nKey="loginError.tooManyAttempts.title" components={{ 1: <br /> }}>
              {'Hai effettuato troppi tentativi di <1 />accesso'}
            </Trans>
          ),
          description: (
            <Trans
              i18nKey="loginError.tooManyAttempts.description"
              components={{ 1: <br />, 3: <br /> }}
            >
              {
                'Hai inserito troppe volte un nome utente o password non corretti. <1 />Verifica i dati di accesso e riprova fra qualche minuto, o contatta il <3 />tuo fornitore di identità SPID per modificare le tue credenziali.'
              }
            </Trans>
          ),
          haveRetryButton: true,
        };
      case '20':
        return {
          title: (
            <Trans i18nKey="loginError.incompatibleCredentials.title" components={{ 1: <br /> }}>
              {'Hai effettuato troppi tentativi di <1 />accesso'}
            </Trans>
          ),
          description: (
            <Trans
              i18nKey="loginError.incompatibleCredentials.description"
              components={{ 1: <br />, 3: <br /> }}
            >
              {
                'Per motivi di sicurezza, devi utilizzare un’identità con un livello di <1 />sicurezza superiore. Per avere più informazioni, contatta il tuo <3 />fornitore di identità SPID.'
              }
            </Trans>
          ),
          haveRetryButton: false,
        };
      case '21':
        return {
          title: t('loginError.authTimeout.title'),
          description: (
            <Trans i18nKey="loginError.authTimeout.description" components={{ 1: <br /> }}>
              {"È passato troppo tempo da quando hai iniziato l'accesso: riparti <1 />dall'inizio."}
            </Trans>
          ),
          haveRetryButton: true,
        };
      case '22':
        return {
          title: (
            <Trans i18nKey="loginError.deniedByUser.title" components={{ 1: <br /> }}>
              {'Non hai dato il consenso all’invio <1 />dei dati'}
            </Trans>
          ),
          description: t('loginError.deniedByUser.description'),
          haveRetryButton: true,
        };
      case '23':
        return {
          title: t('loginError.suspendedOrRevoked.title'),
          description: (
            <Trans i18nKey="loginError.suspendedOrRevoked.description" components={{ 1: <br /> }}>
              {
                'La tua identità SPID risulta sospesa o revocata. Per maggiori <1 />informazioni, contatta il tuo fornitore di identità SPID.'
              }
            </Trans>
          ),
          haveRetryButton: false,
        };
      case '25':
        return {
          title: t('loginError.canceledbyUser.title'),
          description: t('loginError.canceledbyUser.description'),
          haveRetryButton: true,
        };
      default:
        return {
          title: t('loginError.generic.title'),
          description: (
            <Trans i18nKey="loginError.generic.description" components={{ 1: <br /> }}>
              {'Si è verificato un problema durante l’accesso. Riprova tra qualche <1/>minuto.'}
            </Trans>
          ),
          haveRetryButton: true,
        };
    }
  };

  return { handleErrorCode };
};
