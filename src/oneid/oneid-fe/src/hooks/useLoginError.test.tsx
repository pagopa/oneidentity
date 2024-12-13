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
      },
    },
    {
      errorCode: '20',
      expected: {
        title: t('loginError.incompatibleCredentials.title'),
        description: t('loginError.incompatibleCredentials.description'),
      },
    },
    {
      errorCode: '21',
      expected: {
        title: t('loginError.authTimeout.title'),
        description: t('loginError.authTimeout.description'),
      },
    },
    {
      errorCode: '22',
      expected: {
        title: t('loginError.deniedByUser.title'),
        description: t('loginError.deniedByUser.description'),
      },
    },
    {
      errorCode: '23',
      expected: {
        title: t('loginError.suspendedOrRevoked.title'),
        description: t('loginError.suspendedOrRevoked.description'),
      },
    },
    {
      errorCode: '25',
      expected: {
        title: t('loginError.canceledbyUser.title'),
        description: t('loginError.canceledbyUser.description'),
      },
    },
    {
      errorCode: 'unknown',
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
