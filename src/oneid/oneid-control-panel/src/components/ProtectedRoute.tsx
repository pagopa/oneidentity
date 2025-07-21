import { withAuthenticationRequired } from 'react-oidc-context';
import { Dashboard } from '../pages/Dashboard/Dashboard';
import { Customize } from '../pages/Customize/Customize';
import { AddUser } from '../pages/Users/ManageUsers/AddUser';
import { GetUser } from '../pages/Users/GetUsers/GetUsers';

const PrivateRouteDashboard = () => <Dashboard />;
const PrivateRouteCustomize = () => <Customize />;
const PrivateRouteGetUsers = () => <GetUser />;
const PrivateRouteAddUser = () => <AddUser />;

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
export const PrivateGetUsersRoute = withAuthenticationRequired(
  PrivateRouteGetUsers,
  {
    OnRedirecting: () => <div>Redirecting to the manage users page...</div>,
  }
);
export const PrivateAddUsersRoute = withAuthenticationRequired(
  PrivateRouteAddUser,
  {
    OnRedirecting: () => <div>Redirecting to the manage users page...</div>,
  }
);
