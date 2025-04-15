import { withAuthenticationRequired } from 'react-oidc-context';
import { Dashboard } from './Dashboard';

const PrivateRoute = () => <Dashboard />;

export default withAuthenticationRequired(PrivateRoute, {
  OnRedirecting: () => <div>Redirecting to the login page...</div>,
});
