import { renderHook } from '@testing-library/react';
import { t } from 'i18next';

import { useLoginError } from './useLoginError';

describe('useLoginError', () => {
  const cases = [
    {
      errorCode: '19',
      expected: {
        title: t('loginError.tooManyAttempts.title'),
        description: t('loginError.tooManyAttempts.description'),
        haveRetryButton: true,
      },
    },
    {
      errorCode: '20',
      expected: {
        title: t('loginError.incompatibleCredentials.title'),
        description: t('loginError.incompatibleCredentials.description'),
        haveRetryButton: false,
      },
    },
    {
      errorCode: '21',
      expected: {
        title: t('loginError.authTimeout.title'),
        description: t('loginError.authTimeout.description'),
        haveRetryButton: true,
      },
    },
    {
      errorCode: '22',
      expected: {
        title: t('loginError.deniedByUser.title'),
        description: t('loginError.deniedByUser.description'),
        haveRetryButton: true,
      },
    },
    {
      errorCode: '23',
      expected: {
        title: t('loginError.suspendedOrRevoked.title'),
        description: t('loginError.suspendedOrRevoked.description'),
        haveRetryButton: false,
      },
    },
    {
      errorCode: '25',
      expected: {
        title: t('loginError.canceledbyUser.title'),
        description: t('loginError.canceledbyUser.description'),
        haveRetryButton: true,
      },
    },
    {
      errorCode: 'unknown',
      expected: {
        title: t('loginError.generic.title'),
        description: t('loginError.generic.description'),
        haveRetryButton: true,
      },
    },
  ];

  test.each(cases)(
    'should return correct error data for error code $errorCode',
    ({ errorCode, expected }) => {
      const { result } = renderHook(useLoginError);

      const { title, description, haveRetryButton } =
        result.current.handleErrorCode(errorCode);

      expect(title).toEqual(expected.title);
      expect(description).toEqual(expected.description);
      expect(haveRetryButton).toBe(expected.haveRetryButton);
    }
  );
});
