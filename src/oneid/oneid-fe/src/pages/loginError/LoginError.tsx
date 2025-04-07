import { useTranslation } from 'react-i18next';
import { useCallback, useEffect, useState } from 'react';
import { IllusError } from '@pagopa/mui-italia';

import { LoadingOverlay } from '../../components/LoadingOverlay';
import Layout from '../../components/Layout';
import EndingPage from '../../components/EndingPage';
import { redirectToLogin } from '../../utils/utils';
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

  const errorCode = new URLSearchParams(window.location.search).get(
    'errorCode'
  ) as ERROR_CODE;

  const clientRedirecUri = new URLSearchParams(window.location.search).get(
    'redirectUri'
  ) as string;

  const clientRedirectUriSanitized = useCallback((): string => {
    try {
      return decodeURIComponent(clientRedirecUri);
    } catch {
      return '';
    }
  }, [clientRedirecUri]);

  const setContent = useCallback(
    (errorCode: ERROR_CODE) => {
      const { title, description } = handleErrorCode(errorCode);
      setErrorData({ title, description });
      setLoading(false);
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  );

  useEffect(() => {
    if (errorCode) {
      setContent(errorCode);
    }
  }, [setContent, errorCode]);

  const handleRedirect = useCallback(() => {
    if (
      clientRedirectUriSanitized &&
      clientQuery.data?.callbackURI.includes(clientRedirectUriSanitized())
    ) {
      window.location.assign(clientRedirectUriSanitized());
    } else {
      redirectToLogin();
    }
  }, [clientRedirectUriSanitized, clientQuery.data?.callbackURI]);

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
        variantButton="contained"
        labelButton={t('loginError.close')}
        onClickButton={handleRedirect}
      />
    </Layout>
  );
};
