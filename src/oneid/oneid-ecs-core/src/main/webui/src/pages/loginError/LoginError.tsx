import { Trans, useTranslation } from 'react-i18next';
import { useEffect, useState } from 'react';
import { IllusError } from '@pagopa/mui-italia';
import { LoadingOverlay } from '../../components/LoadingOverlay';
import Layout from '../../components/Layout';
import EndingPage from '../../components/EndingPage';
import { redirectToLogin } from '../../utils/utils';

const LoginError = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState<boolean>(true);
  const [title, setTitle] = useState<string | React.ReactElement>();
  const [description, setDescription] = useState<string | React.ReactElement>();
  const [haveRetryButton, setHaveRetryButton] = useState<boolean>();

  const errorCode = new URLSearchParams(window.location.search).get('errorCode');
  console.log(title, description, haveRetryButton);

  useEffect(() => {
    if (errorCode) {
      handleError(errorCode);
    }
  }, [errorCode]);

  const handleError = (errorCode: string) => {
    setLoading(true);
    switch (errorCode) {
      case '19': {
        setTitle(
          <Trans i18nKey="loginError.tooManyAttempts.title" components={{ 1: <br /> }}>
            {'Hai effettuato troppi tentativi di <1 />accesso'}
          </Trans>
        );
        setDescription(
          <Trans
            i18nKey="loginError.tooManyAttempts.description"
            components={{ 1: <br />, 3: <br /> }}
          >
            {
              'Hai inserito troppe volte un nome utente o password non corretti. <1 />Verifica i dati di accesso e riprova fra qualche minuto, o contatta il <3 />tuo fornitore di identità SPID per modificare le tue credenziali.'
            }
          </Trans>
        );
        setHaveRetryButton(true);
        break;
      }
      case '20': {
        setTitle(
          <Trans i18nKey="loginError.incompatibleCredentials.title" components={{ 1: <br /> }}>
            {'Hai effettuato troppi tentativi di <1 />accesso'}
          </Trans>
        );
        setDescription(
          <Trans
            i18nKey="loginError.incompatibleCredentials.description"
            components={{ 1: <br />, 3: <br /> }}
          >
            {
              'Per motivi di sicurezza, devi utilizzare un’identità con un livello di <1 />sicurezza superiore. Per avere più informazioni, contatta il tuo <3 />fornitore di identità SPID.'
            }
          </Trans>
        );
        setHaveRetryButton(false);
        break;
      }
      case '21': {
        setTitle(t('loginError.authTimeout.title'));
        setDescription(
          <Trans i18nKey="loginError.authTimeout.description" components={{ 1: <br /> }}>
            {"È passato troppo tempo da quando hai iniziato l'accesso: riparti <1 />dall'inizio."}
          </Trans>
        );
        setHaveRetryButton(true);
        break;
      }
      case '22': {
        setTitle(
          <Trans i18nKey="loginError.deniedByUser.title" components={{ 1: <br /> }}>
            {'Non hai dato il consenso all’invio <1 />dei dati'}
          </Trans>
        );
        setDescription(t('loginError.deniedByUser.description'));
        setHaveRetryButton(true);
        break;
      }
      case '23': {
        setTitle(t('loginError.suspendedOrRevoked.title'));
        setDescription(
          <Trans i18nKey="loginError.suspendedOrRevoked.description" components={{ 1: <br /> }}>
            {
              'La tua identità SPID risulta sospesa o revocata. Per maggiori <1 />informazioni, contatta il tuo fornitore di identità SPID.'
            }
          </Trans>
        );
        setHaveRetryButton(false);
        break;
      }
      case '25': {
        setTitle(t('loginError.canceledbyUser.title'));
        setDescription(t('loginError.canceledbyUser.description'));
        setHaveRetryButton(true);
        break;
      }
      default: {
        setTitle(t('loginError.generic.title'));
        setDescription(
          <Trans i18nKey="loginError.generic.description" components={{ 1: <br /> }}>
            {'Si è verificato un problema durante l’accesso. Riprova tra qualche <1/>minuto.'}
          </Trans>
        );
        setHaveRetryButton(true);
        break;
      }
    }

    // const spidId = storageSpidSelectedOps.read();
    // trackEvent('LOGIN_FAILURE', { reason: errorCode, SPID_IDP_ID: spidId });
    console.error(`login unsuccessfull! error code obtained from idp: ${errorCode}`);
    setLoading(false);
  };

  return loading ? (
    <LoadingOverlay loadingText="" />
  ) : (
    <Layout>
      {/* TODO add footer */}
      <EndingPage
        minHeight={'100vh'}
        icon={<IllusError size={60} />}
        variantTitle="h4"
        variantDescription="body1"
        title={title}
        description={description}
        variantFirstButton={haveRetryButton ? 'outlined' : 'contained'}
        variantSecondButton="contained"
        buttonLabel={t('loginError.close')}
        secondButtonLabel={t('loginError.retry')}
        onButtonClick={redirectToLogin}
        onSecondButtonClick={() => history.go(-1)}
        haveTwoButtons={haveRetryButton}
      />
    </Layout>
  );
};

export default LoginError;
