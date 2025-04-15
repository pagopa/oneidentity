import { Routes, Route, BrowserRouter } from 'react-router-dom';
import { LoginForm } from './components/LoginForm';
import ProtectedRoute from './components/ProtectedRoute';
import { AuthProvider, AuthProviderProps } from 'react-oidc-context';
import { ENV } from './utils/env';
import { User } from 'oidc-client-ts';

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
  // no silent renew via "prompt=none" (https://github.com/authts/oidc-client-ts/issues/366)
  automaticSilentRenew: false,
  onSigninCallback,
};

function App() {
  return (
    <AuthProvider {...oidcConfig}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LoginForm />} />
          <Route path="/dashboard" element={<ProtectedRoute />} />
          <Route path="/dashboard/:client_id" element={<ProtectedRoute />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
