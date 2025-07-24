import { withAuthenticationRequired } from 'react-oidc-context';
import { Dashboard } from '../pages/Dashboard/Dashboard';
import { Customize } from '../pages/Customize/Customize';
import { AddOrUpdateUser } from '../pages/Users/ManageUsers/AddOrUpdateUser';
import { GetUserList } from '../pages/Users/GetUsers/GetUserList';

const PrivateRouteDashboard = () => <Dashboard />;
const PrivateRouteCustomize = () => <Customize />;
const PrivateRouteGetUsers = () => <GetUserList />;
const PrivateRouteAddOrUpdateUser = () => <AddOrUpdateUser />;

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
export const PrivateAddOrUpdateUsersRoute = withAuthenticationRequired(
  PrivateRouteAddOrUpdateUser,
  {
    OnRedirecting: () => <div>Redirecting to the manage users page...</div>,
  }
);
