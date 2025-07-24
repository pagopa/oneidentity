import { Route, Routes } from 'react-router-dom';
import { LoginForm } from './pages/Login/Login';
import {
  PrivateAddOrUpdateUsersRoute,
  PrivateCustomizedRoute,
  PrivateDashboardRoute,
  PrivateGetUsersRoute,
} from './components/ProtectedRoute';
import { ROUTE_PATH } from './utils/constants';

const AppRoutes = () => {
  return (
    <Routes>
      <Route path={ROUTE_PATH.LOGIN} element={<LoginForm />} />
      <Route
        path={`${ROUTE_PATH.DASHBOARD}/:client_id?`}
        element={<PrivateDashboardRoute />}
      />
      <Route
        path={`${ROUTE_PATH.CUSTOMIZE}/:client_id`}
        element={<PrivateCustomizedRoute />}
      />
      <Route path={ROUTE_PATH.USER_LIST} element={<PrivateGetUsersRoute />} />
      <Route
        path={`${ROUTE_PATH.USER}/user/:id?`}
        element={<PrivateAddOrUpdateUsersRoute />}
      />
    </Routes>
  );
};

export default AppRoutes;
