import {
  ROUTE_LOGIN,
  ROUTE_LOGIN_ERROR,
  ROUTE_LOGOUT,
} from './utils/constants';
import { redirectToLogin } from './utils/utils';
import Logout from './pages/logout/Logout';
import { LoginError } from './pages/loginError/LoginError';
import Login from './pages/login';

function App() {
  switch (window.location.pathname) {
    case ROUTE_LOGIN:
      return <Login />;
    case ROUTE_LOGIN_ERROR:
      return <LoginError />;
    case ROUTE_LOGOUT:
      return <Logout />;
    default:
      redirectToLogin();
      return;
  }
}

export default App;
