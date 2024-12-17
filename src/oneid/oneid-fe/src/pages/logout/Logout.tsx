import { redirectToLogin } from '../../utils/utils';

const Logout = () => {
  redirectToLogin();
  return <div />;
};

export default Logout;
