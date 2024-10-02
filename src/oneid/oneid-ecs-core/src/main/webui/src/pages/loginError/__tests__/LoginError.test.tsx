import { render, waitFor, screen } from '@testing-library/react';
import LoginError from '../LoginError';
import { ROUTE_LOGIN_ERROR } from '../../../utils/constants';
import './../../../locale';
import { vi } from 'vitest';

const oldWindowLocation = global.window.location;

beforeAll(() => {
  Object.defineProperty(window, 'location', { value: { assign: vi.fn() } });
});
afterAll(() => {
  Object.defineProperty(window, 'location', { value: oldWindowLocation });
});

test('Test: Identity providers authentication errors and the relative error pages', async () => {
  let componentRendered = false;
  const errorCodes = ['19', '20', '21', '22', '23', '25', '24'];

  errorCodes.forEach((errorCode) => {
    if (!componentRendered) {
      render(<LoginError />);

      window.location.assign(ROUTE_LOGIN_ERROR.concat(`?errorCode=${errorCode}`));

      switch (errorCode) {
        case '19':
          waitFor(() => {
            screen.getByText(/Hai effettuato troppi tentativi di accesso/);
            screen.getByText(
              /Hai inserito troppe volte un nome utente o password non corretti. Verifica i dati di accesso e riprova fra qualche minuto, o contatta il tuo fornitore di identità SPID per modificare le tue credenziali./
            );
            screen.getByText('Riprova');
            screen.getByText('Chiudi');
          });
          break;
        case '20':
          screen.getByText(/Hai effettuato troppi tentativi di accesso/);
          screen.getByText(
            /Per motivi di sicurezza, devi utilizzare un’identità con un livello di sicurezza superiore. Per avere più informazioni, contatta il tuo fornitore di identità SPID./
          );
          screen.getByText('Chiudi');
          break;
        case '21':
          screen.getByText(
            /È passato troppo tempo da quando hai iniziato l'accesso: riparti dall'inizio./
          );
          screen.getByText(/Ti chiediamo di riprovare più tardi./);
          screen.getByText('Riprova');
          screen.getByText('Chiudi');
          break;
        case '22':
          screen.getByText(/Non hai dato il consenso all’invio dei dati/);
          screen.getByText(/Per accedere, è necessario acconsentire all’invio di alcuni dati./);
          screen.getByText('Riprova');
          screen.getByText('Chiudi');
          break;
        case '23':
          screen.getByText(/Identità sospesa o revocata/);
          screen.getByText(
            /La tua identità SPID risulta sospesa o revocata. Per maggiori informazioni, contatta il tuo fornitore di identità SPID./
          );
          screen.getByText('Chiudi');
          break;
        case '25':
          screen.getByText(/Hai annullato l’accesso/);
          screen.getByText(/Per entrare, riprova quando vuoi./);
          screen.getByText('Riprova');
          screen.getByText('Chiudi');
          break;
        default:
          screen.getByText(/Non è stato possibile accedere/);
          screen.getByText(
            /Si è verificato un problema durante l’accesso. Riprova tra qualche minuto./
          );
          screen.getByText('Riprova');
          screen.getByText('Chiudi');
      }
    }
    componentRendered = true;
  });
});
