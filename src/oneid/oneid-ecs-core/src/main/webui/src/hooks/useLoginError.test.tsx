import { renderHook } from '@testing-library/react';
import { Trans } from 'react-i18next';
import { t } from 'i18next';
import { useLoginError } from './useLoginError';

describe('useLoginError', () => {
  const cases = [
    {
      errorCode: '19',
      expected: {
        title: (
          <Trans i18nKey="loginError.tooManyAttempts.title" components={{ 1: <br /> }}>
            {'Hai effettuato troppi tentativi di <1 />accesso'}
          </Trans>
        ),
        description: (
          <Trans
            i18nKey="loginError.tooManyAttempts.description"
            components={{ 1: <br />, 3: <br /> }}
          >
            {
              'Hai inserito troppe volte un nome utente o password non corretti. <1 />Verifica i dati di accesso e riprova fra qualche minuto, o contatta il <3 />tuo fornitore di identità SPID per modificare le tue credenziali.'
            }
          </Trans>
        ),
        haveRetryButton: true,
      },
    },
    {
      errorCode: '20',
      expected: {
        title: (
          <Trans i18nKey="loginError.incompatibleCredentials.title" components={{ 1: <br /> }}>
            {'Hai effettuato troppi tentativi di <1 />accesso'}
          </Trans>
        ),
        description: (
          <Trans
            i18nKey="loginError.incompatibleCredentials.description"
            components={{ 1: <br />, 3: <br /> }}
          >
            {
              'Per motivi di sicurezza, devi utilizzare un’identità con un livello di <1 />sicurezza superiore. Per avere più informazioni, contatta il tuo <3 />fornitore di identità SPID.'
            }
          </Trans>
        ),
        haveRetryButton: false,
      },
    },
    {
      errorCode: '21',
      expected: {
        title: t('loginError.authTimeout.title'),
        description: (
          <Trans i18nKey="loginError.authTimeout.description" components={{ 1: <br /> }}>
            {"È passato troppo tempo da quando hai iniziato l'accesso: riparti <1 />dall'inizio."}
          </Trans>
        ),
        haveRetryButton: true,
      },
    },
    {
      errorCode: '22',
      expected: {
        title: (
          <Trans i18nKey="loginError.deniedByUser.title" components={{ 1: <br /> }}>
            {'Non hai dato il consenso all’invio <1 />dei dati'}
          </Trans>
        ),
        description: t('loginError.deniedByUser.description'),
        haveRetryButton: true,
      },
    },
    {
      errorCode: '23',
      expected: {
        title: t('loginError.suspendedOrRevoked.title'),
        description: (
          <Trans i18nKey="loginError.suspendedOrRevoked.description" components={{ 1: <br /> }}>
            {
              'La tua identità SPID risulta sospesa o revocata. Per maggiori <1 />informazioni, contatta il tuo fornitore di identità SPID.'
            }
          </Trans>
        ),
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
        description: (
          <Trans i18nKey="loginError.generic.description" components={{ 1: <br /> }}>
            {'Si è verificato un problema durante l’accesso. Riprova tra qualche <1/>minuto.'}
          </Trans>
        ),
        haveRetryButton: true,
      },
    },
  ];

  test.each(cases)(
    'should return correct error data for error code $errorCode',
    ({ errorCode, expected }) => {
      const { result } = renderHook(() => useLoginError());

      const { title, description, haveRetryButton } = result.current.handleErrorCode(errorCode);

      expect(title).toEqual(expected.title);
      expect(description).toEqual(expected.description);
      expect(haveRetryButton).toBe(expected.haveRetryButton);
    }
  );
});
