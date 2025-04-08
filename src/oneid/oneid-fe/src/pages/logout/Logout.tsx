import { redirectToLoginWithParams } from '../../utils/utils';

const Logout = () => {
  redirectToLoginWithParams();
  return <div />;
};

export default Logout;
