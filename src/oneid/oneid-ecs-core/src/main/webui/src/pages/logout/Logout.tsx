import { redirectToLogin } from '../../utils/utils';

const Logout = () => {
  // storageOnSuccessOps.delete();
  // storageTokenOps.delete();
  // storageUserOps.delete();
  redirectToLogin();

  return <div />;
};

export default Logout;
