import {
  ROUTE_LOGIN,
  ROUTE_LOGIN_ERROR,
  ROUTE_LOGOUT,
} from './utils/constants';
import { redirectToLogin } from './utils/utils';
import Logout from './pages/logout/Logout';
import { LoginError } from './pages/loginError/LoginError';
import Login from './pages/login';

const onLogout = () => <Logout />;
const onLoginError = () => <LoginError />;
const onLoginRequest = () => <Login />;

function App() {
  if (window.location.pathname === ROUTE_LOGOUT) {
    return onLogout();
  } else {
    switch (window.location.pathname) {
      case ROUTE_LOGIN:
        return onLoginRequest();
      case ROUTE_LOGIN_ERROR:
        return onLoginError();
      default:
        redirectToLogin();
    }
  }

  return <div />;
}

export default App;
