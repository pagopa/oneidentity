import { withAuthenticationRequired } from 'react-oidc-context';
import { Dashboard } from '../pages/Dashboard/Dashboard';
import { Customize } from '../pages/Customize/Customize';

const PrivateRouteDashboard = () => <Dashboard />;
const PrivateRouteCustomize = () => <Customize />;

export const PrivateDashboardRoute = withAuthenticationRequired(
  PrivateRouteDashboard,
  {
    OnRedirecting: () => <div>Redirecting to the login page...</div>,
  }
);
export const PrivateCustomizedRoute = withAuthenticationRequired(
  PrivateRouteCustomize,
  {
    OnRedirecting: () => <div>Redirecting to the login page...</div>,
  }
);
