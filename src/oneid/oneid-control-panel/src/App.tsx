import { Routes, Route, BrowserRouter } from 'react-router-dom';
import { LoginForm } from './pages/Login/Login';
import {
  PrivateAddUsersRoute,
  PrivateCustomizedRoute,
  PrivateDashboardRoute,
  PrivateGetUsersRoute,
} from './components/ProtectedRoute';
import { AuthProvider, AuthProviderProps } from 'react-oidc-context';
import { ENV } from './utils/env';
import { User } from 'oidc-client-ts';
import Layout from './components/Layout';

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
        <Layout>
          <Routes>
            <Route path="/" element={<LoginForm />} />
            <Route
              path="/dashboard/:client_id?"
              element={<PrivateDashboardRoute />}
            />
            <Route
              path="/dashboard/customize/:client_id"
              element={<PrivateCustomizedRoute />}
            />
            <Route path="/dashboard/users" element={<PrivateGetUsersRoute />} />
            <Route
              path="/dashboard/addUsers"
              element={<PrivateAddUsersRoute />}
            />
          </Routes>
        </Layout>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
