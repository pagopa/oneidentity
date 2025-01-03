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

export const LoginError = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState<boolean>(true);
  const [errorData, setErrorData] = useState<ErrorData | undefined>(undefined);

  const errorCode = new URLSearchParams(window.location.search).get(
    'errorCode'
  ) as ERROR_CODE;

  const { handleErrorCode } = useLoginError();

  const setContent = useCallback(
    (errorCode: ERROR_CODE) => {
      const { title, description } = handleErrorCode(errorCode);
      setErrorData({ title, description });
      setLoading(false);
    },
    [handleErrorCode]
  );

  useEffect(() => {
    setTimeout(() => {
      setContent(ERROR_CODE.GENERIC);
    }, 5 * 1000);
  }, [setContent]);

  useEffect(() => {
    if (errorCode) {
      setContent(errorCode);
    }
  }, [setContent, errorCode]);

  return loading || !errorData ? (
    <LoadingOverlay loadingText="" />
  ) : (
    <Layout>
      {/* TODO add footer */}
      <EndingPage
        minHeight={'100vh'}
        icon={<IllusError size={60} />}
        variantTitle="h4"
        variantDescription="body1"
        title={errorData.title}
        description={errorData.description}
        variantButton="contained"
        labelButton={t('loginError.close')}
        onClickButton={redirectToLogin}
      />
    </Layout>
  );
};
