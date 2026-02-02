import { BrowserRouter } from 'react-router-dom';
import { AuthProvider, AuthProviderProps, useAuth } from 'react-oidc-context';
import { ENV } from './utils/env';
import { User } from 'oidc-client-ts';
import Layout from './components/Layout';
import AppRoutes from './Routes';
import { ClientIdProvider } from './context/ClientIdContext';
import { NotificationProvider } from './context/NotificationContext';
import { useEffect } from 'react';
import { setAuthInstance } from './context/AuthInterceptorContext';

// remove ?code... in params after signin
const onSigninCallback = (_user: User | undefined): void => {
  console.log('User signed in:', _user);

  window.history.replaceState({}, document.title, window.location.pathname);
};

const oidcConfig: AuthProviderProps = {
  authority: ENV.OIDC.API,
  client_id: ENV.OIDC.CLIENT_ID,
  redirect_uri: ENV.OIDC.REDIRECT_URI,
  response_type: ENV.OIDC.RESPONSE_TYPE,
  post_logout_redirect_uri: ENV.OIDC.REDIRECT_URI,
  scope: ENV.OIDC.SCOPE,
  // no revoke of "access token" (https://github.com/authts/oidc-client-ts/issues/262)
  revokeTokenTypes: ['refresh_token'],
  // enable silent renew (refresh access and id token automatically)
  automaticSilentRenew: true,
  onSigninCallback,
};

function AuthBridge() {
  const auth = useAuth();

  useEffect(() => {
    setAuthInstance(auth);
  }, [auth]);

  return null;
}

function App() {
  return (
    <AuthProvider {...oidcConfig}>
      <AuthBridge />
      <BrowserRouter>
        <ClientIdProvider>
          <NotificationProvider>
            <Layout>
              <AppRoutes />
            </Layout>
          </NotificationProvider>
        </ClientIdProvider>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
