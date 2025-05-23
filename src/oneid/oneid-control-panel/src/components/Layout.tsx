import { AppBar, Box, Button, Toolbar, Typography } from '@mui/material';
import { ENV } from '../utils/env';
import { useAuth } from 'react-oidc-context';

type Props = {
  children: React.ReactNode;
};

const Layout = ({ children }: Props) => {
  const { removeUser, signoutRedirect } = useAuth();
  const handleLogout = () => {
    removeUser();
    return signoutRedirect({
      // aws cognito extras
      extraQueryParams: {
        client_id: ENV.OIDC.CLIENT_ID,
        logout_uri: ENV.OIDC.REDIRECT_URI,
        redirect_uri: ENV.OIDC.REDIRECT_URI,
        response_type: ENV.OIDC.RESPONSE_TYPE,
        scope: ENV.OIDC.SCOPE,
      },
    });
  };

  return (
    <Box
      bgcolor={'#F5F5F5'}
      margin={0}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
      }}
    >
      <AppBar position="static">
        <Toolbar>
          <Typography
            variant="h6"
            component="div"
            color={'white'}
            sx={{ flexGrow: 1 }}
          >
            OneIdentity Client Management
          </Typography>
          <Button
            color="inherit"
            onClick={handleLogout}
            data-testid="logout-button"
          >
            Logout
          </Button>
        </Toolbar>
      </AppBar>
      {children}
    </Box>
  );
};

export default Layout;
