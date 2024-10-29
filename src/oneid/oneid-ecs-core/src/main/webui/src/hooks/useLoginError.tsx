import { t } from 'i18next';

export type ErrorData = {
  title: string;
  description: string;
  haveRetryButton: boolean;
};

export const useLoginError = () => {
  const handleErrorCode = (errorCode: string): ErrorData => {
    switch (errorCode) {
      case '19':
        return {
          title: t('loginError.tooManyAttempts.title'),
          description: t('loginError.tooManyAttempts.description'),
          haveRetryButton: true,
        };
      case '20':
        return {
          title: t('loginError.incompatibleCredentials.title'),
          description: t('loginError.incompatibleCredentials.description'),
          haveRetryButton: false,
        };
      case '21':
        return {
          title: t('loginError.authTimeout.title'),
          description: t('loginError.authTimeout.description'),
          haveRetryButton: true,
        };
      case '22':
        return {
          title: t('loginError.deniedByUser.title'),
          description: t('loginError.deniedByUser.description'),
          haveRetryButton: true,
        };
      case '23':
        return {
          title: t('loginError.suspendedOrRevoked.title'),
          description: t('loginError.suspendedOrRevoked.description'),
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
          description: t('loginError.generic.description'),
          haveRetryButton: true,
        };
    }
  };

  return { handleErrorCode };
};
