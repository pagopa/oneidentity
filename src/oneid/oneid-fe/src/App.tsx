import {
  ROUTE_COURTESY_SERVICE_KO,
  ROUTE_COURTESY_SWITCHTOIO,
  ROUTE_LOGIN,
  ROUTE_LOGIN_ERROR,
  ROUTE_LOGOUT,
} from './utils/constants';
import { redirectToLoginWithParams } from './utils/utils';
import Logout from './pages/logout/Logout';
import { LoginError } from './pages/loginError/LoginError';
import Login from './pages/login';
import './global.css';
import { SwitchToIO } from './pages/courtesy/SwitchToIO';
import { ServiceKO } from './pages/courtesy/ServiceKO';

function App() {
  switch (window.location.pathname) {
    case ROUTE_LOGIN:
      return <Login />;
    case ROUTE_LOGIN_ERROR:
      return <LoginError />;
    case ROUTE_LOGOUT:
      return <Logout />;
    case ROUTE_COURTESY_SWITCHTOIO:
      return <SwitchToIO />;
    case ROUTE_COURTESY_SERVICE_KO:
      return <ServiceKO />;
    default:
      redirectToLoginWithParams();
      return;
  }
}

export default App;
