import { Navigate, Route, Routes } from 'react-router-dom';
import { LoginForm } from './pages/Login/Login';
import {
  PrivateAddOrUpdateUsersRoute,
  PrivateCustomizedRoute,
  PrivateDashboardRoute,
  PrivateGetUsersRoute,
} from './components/ProtectedRoute';
import { ROUTE_PATH } from './utils/constants';
import { ENV } from './utils/env';

const AppRoutes = () => {
  return (
    <Routes>
      <Route path={ROUTE_PATH.LOGIN} element={<LoginForm />} />
      <Route path={ROUTE_PATH.DASHBOARD} element={<PrivateDashboardRoute />} />
      <Route path={ROUTE_PATH.CUSTOMIZE} element={<PrivateCustomizedRoute />} />
      {ENV.CURRENT_ENV !== 'prod' && (
        <>
          <Route
            path={ROUTE_PATH.USER_LIST}
            element={<PrivateGetUsersRoute />}
          />
          <Route
            path={`${ROUTE_PATH.USER}/:id?`}
            element={<PrivateAddOrUpdateUsersRoute />}
          />
        </>
      )}
      <Route
        path="*"
        element={<Navigate to={ROUTE_PATH.DASHBOARD} replace />}
      />
    </Routes>
  );
};

export default AppRoutes;
