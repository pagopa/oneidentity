import { renderHook } from '@testing-library/react';
import { t } from 'i18next';
import { ERROR_CODE, ErrorData, useLoginError } from './useLoginError';

describe('useLoginError', () => {
  const cases: Array<{
    errorCode: ERROR_CODE;
    expected: ErrorData;
  }> = [
    {
      errorCode: ERROR_CODE.TOO_MANY_ATTEMPTS,
      expected: {
        title: t('loginError.tooManyAttempts.title'),
        description: t('loginError.tooManyAttempts.description'),
      },
    },
    {
      errorCode: ERROR_CODE.INCOMPATIBLE_CREDENTIALS,
      expected: {
        title: t('loginError.incompatibleCredentials.title'),
        description: t('loginError.incompatibleCredentials.description'),
      },
    },
    {
      errorCode: ERROR_CODE.AUTH_TIMEOUT,
      expected: {
        title: t('loginError.authTimeout.title'),
        description: t('loginError.authTimeout.description'),
      },
    },
    {
      errorCode: ERROR_CODE.DENIED_BY_USER,
      expected: {
        title: t('loginError.deniedByUser.title'),
        description: t('loginError.deniedByUser.description'),
      },
    },
    {
      errorCode: ERROR_CODE.SUSPENDED_OR_REVOKED,
      expected: {
        title: t('loginError.suspendedOrRevoked.title'),
        description: t('loginError.suspendedOrRevoked.description'),
      },
    },
    {
      errorCode: ERROR_CODE.CANCELED_BY_USER,
      expected: {
        title: t('loginError.canceledbyUser.title'),
        description: t('loginError.canceledbyUser.description'),
      },
    },
    {
      errorCode: ERROR_CODE.MISSING_IDP,
      expected: {
        title: t('loginError.generic.title'),
        description: t('loginError.generic.description'),
      },
    },
  ];

  test.each(cases)(
    'should return correct error data for error code $errorCode',
    ({ errorCode, expected }) => {
      const { result } = renderHook(useLoginError);

      const { title, description } = result.current.handleErrorCode(errorCode);

      expect(title).toEqual(expected.title);
      expect(description).toEqual(expected.description);
    }
  );
});
