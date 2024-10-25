import { useTranslation } from 'react-i18next';
import { useEffect, useState } from 'react';
import { IllusError } from '@pagopa/mui-italia';
import { LoadingOverlay } from '../../components/LoadingOverlay';
import Layout from '../../components/Layout';
import EndingPage from '../../components/EndingPage';
import { redirectToLogin } from '../../utils/utils';
import { ErrorData, useLoginError } from '../../hooks/useLoginError';

export const LoginError = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState<boolean>(true);
  const [errorData, setErrorData] = useState<ErrorData | undefined>(undefined);

  const errorCode = new URLSearchParams(window.location.search).get('errorCode');

  const { handleErrorCode } = useLoginError();

  useEffect(() => {
    if (errorCode) {
      setLoading(true);
      const { title, description, haveRetryButton } = handleErrorCode(errorCode);
      setErrorData({ title, description, haveRetryButton });
      setLoading(false);
    }
  }, [errorCode]);

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
        variantFirstButton={errorData.haveRetryButton ? 'outlined' : 'contained'}
        variantSecondButton="contained"
        buttonLabel={t('loginError.close')}
        secondButtonLabel={t('loginError.retry')}
        onButtonClick={redirectToLogin}
        onSecondButtonClick={() => history.go(-1)}
        haveTwoButtons={errorData.haveRetryButton}
      />
    </Layout>
  );
};
