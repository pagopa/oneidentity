import { t } from 'i18next';

export type ErrorData = {
  title: string;
  description: string;
};

export const useLoginError = () => {
  const handleErrorCode = (errorCode: string): ErrorData => {
    switch (errorCode) {
      case '19':
        return {
          title: t('loginError.tooManyAttempts.title'),
          description: t('loginError.tooManyAttempts.description'),
        };
      case '20':
        return {
          title: t('loginError.incompatibleCredentials.title'),
          description: t('loginError.incompatibleCredentials.description'),
        };
      case '21':
        return {
          title: t('loginError.authTimeout.title'),
          description: t('loginError.authTimeout.description'),
        };
      case '22':
        return {
          title: t('loginError.deniedByUser.title'),
          description: t('loginError.deniedByUser.description'),
        };
      case '23':
        return {
          title: t('loginError.suspendedOrRevoked.title'),
          description: t('loginError.suspendedOrRevoked.description'),
        };
      case '25':
        return {
          title: t('loginError.canceledbyUser.title'),
          description: t('loginError.canceledbyUser.description'),
        };
      default:
        return {
          title: t('loginError.generic.title'),
          description: t('loginError.generic.description'),
        };
    }
  };

  return { handleErrorCode };
};
