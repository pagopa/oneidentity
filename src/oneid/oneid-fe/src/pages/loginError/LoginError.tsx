import { useTranslation } from 'react-i18next';
import { useCallback, useEffect, useState } from 'react';
import { IllusError } from '@pagopa/mui-italia';

import { LoadingOverlay } from '../../components/LoadingOverlay';
import Layout from '../../components/Layout';
import EndingPage from '../../components/EndingPage';
import {
  redirectToClientWithError,
  redirectToLogin,
  redirectToLoginToRetry,
} from '../../utils/utils';
import {
  ERROR_CODE,
  ErrorData,
  useLoginError,
} from '../../hooks/useLoginError';
import { useLoginData } from '../../hooks/useLoginData';

export const LoginError = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState<boolean>(true);
  const [errorData, setErrorData] = useState<ErrorData | undefined>(undefined);
  const { clientQuery } = useLoginData();
  const { handleErrorCode } = useLoginError();

  const errorCode = (new URLSearchParams(window.location.search).get(
    'error_code'
  ) || ERROR_CODE.GENERIC) as ERROR_CODE;

  const clientRedirectUri = new URLSearchParams(window.location.search).get(
    'redirect_uri'
  );

  const state = new URLSearchParams(window.location.search).get('state');

  const setContent = useCallback(
    (errorCode: ERROR_CODE) => {
      const { title, description, haveRetryButton } =
        handleErrorCode(errorCode);
      // disable retry button if there are no stored OIDC parameters to retry the login flow
      const canRetry = haveRetryButton && redirectToLoginToRetry() !== null;
      setErrorData({ title, description, haveRetryButton: canRetry });
      setLoading(false);
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  );

  useEffect(() => {
    if (errorCode) {
      setContent(errorCode);
    } else {
      // fallback if errorCode is not present
      setContent(ERROR_CODE.GENERIC);
    }
  }, [setContent, errorCode]);

  const handleRedirect = useCallback(() => {
    if (
      clientRedirectUri &&
      clientQuery.data?.callbackURI?.includes(clientRedirectUri)
    ) {
      let route: string;
      try {
        route = redirectToClientWithError(errorCode, clientRedirectUri, state);
      } catch {
        redirectToLogin();
        return;
      }
      window.location.assign(route);
    } else {
      redirectToLogin();
    }
  }, [clientRedirectUri, clientQuery.data?.callbackURI, errorCode, state]);

  const handleRetry = useCallback(() => {
    const route = redirectToLoginToRetry();
    if (errorData?.haveRetryButton && route) {
      window.location.assign(route);
    }
  }, [errorData]);

  return loading || !errorData ? (
    <LoadingOverlay loadingText="" />
  ) : (
    <Layout>
      <EndingPage
        icon={<IllusError size={60} />}
        variantTitle="h4"
        variantDescription="body1"
        title={errorData.title}
        description={errorData.description}
        variantButton={errorData.haveRetryButton ? 'outlined' : 'contained'}
        labelButton={t('loginError.close')}
        onClickButton={handleRedirect}
        secondLabelButton={t('loginError.retry')}
        secondVariantButton="contained"
        onSecondButtonClick={handleRetry}
        haveTwoButtons={errorData.haveRetryButton}
      />
    </Layout>
  );
};
