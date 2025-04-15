import i18next from '../locale';
const { t } = i18next;

export type ErrorData = {
  title: string;
  description: string;
  haveRetryButton: boolean;
};

export enum ERROR_CODE {
  TOO_MANY_ATTEMPTS = '19',
  INCOMPATIBLE_CREDENTIALS = '20',
  AUTH_TIMEOUT = '21',
  DENIED_BY_USER = '22',
  SUSPENDED_OR_REVOKED = '23',
  CANCELED_BY_USER = '25',
  ID_NOT_SUPPORTED = '30',
  MISSING_RESPONSE_TYPE = 'AUTHORIZATION_ERROR_RESPONSE_TYPE',
  MISSING_CLIENT_ID = 'GENERIC_HTML_ERROR',
  MISSING_IDP = 'AUTHORIZATION_ERROR_IDP',
  IDP_ERROR = 'IDP_ERROR',
  OI_ERROR = 'OI_ERROR',
  SESSION_ERROR = 'SESSION_ERROR',
  MISSING_REDIRECT_URI = 'CALLBACK_URI_NOT_FOUND',
  GENERIC = 'GENERIC',
}

export const GENERIC_ERROR_DATA = {
  title: t('loginError.generic.title'),
  description: t('loginError.generic.description'),
  haveRetryButton: true,
};

export const erroMap: Record<ERROR_CODE, ErrorData> = {
  [ERROR_CODE.TOO_MANY_ATTEMPTS]: {
    title: t('loginError.tooManyAttempts.title'),
    description: t('loginError.tooManyAttempts.description'),
    haveRetryButton: true,
  },
  [ERROR_CODE.INCOMPATIBLE_CREDENTIALS]: {
    title: t('loginError.incompatibleCredentials.title'),
    description: t('loginError.incompatibleCredentials.description'),
    haveRetryButton: false,
  },
  [ERROR_CODE.AUTH_TIMEOUT]: {
    title: t('loginError.authTimeout.title'),
    description: t('loginError.authTimeout.description'),
    haveRetryButton: true,
  },
  [ERROR_CODE.DENIED_BY_USER]: {
    title: t('loginError.deniedByUser.title'),
    description: t('loginError.deniedByUser.description'),
    haveRetryButton: true,
  },
  [ERROR_CODE.SUSPENDED_OR_REVOKED]: {
    title: t('loginError.suspendedOrRevoked.title'),
    description: t('loginError.suspendedOrRevoked.description'),
    haveRetryButton: false,
  },
  [ERROR_CODE.CANCELED_BY_USER]: {
    title: t('loginError.canceledbyUser.title'),
    description: t('loginError.canceledbyUser.description'),
    haveRetryButton: true,
  },
  [ERROR_CODE.ID_NOT_SUPPORTED]: GENERIC_ERROR_DATA,
  [ERROR_CODE.MISSING_RESPONSE_TYPE]: GENERIC_ERROR_DATA,
  [ERROR_CODE.MISSING_CLIENT_ID]: GENERIC_ERROR_DATA,
  [ERROR_CODE.MISSING_IDP]: GENERIC_ERROR_DATA,
  [ERROR_CODE.IDP_ERROR]: GENERIC_ERROR_DATA,
  [ERROR_CODE.OI_ERROR]: GENERIC_ERROR_DATA,
  [ERROR_CODE.SESSION_ERROR]: GENERIC_ERROR_DATA,
  [ERROR_CODE.MISSING_REDIRECT_URI]: GENERIC_ERROR_DATA,
  [ERROR_CODE.GENERIC]: GENERIC_ERROR_DATA,
};

export const useLoginError = () => {
  const handleErrorCode = (errorCode: ERROR_CODE): ErrorData => {
    if (errorCode in erroMap) {
      return erroMap[errorCode];
    } else {
      return GENERIC_ERROR_DATA;
    }
  };

  return { handleErrorCode };
};
