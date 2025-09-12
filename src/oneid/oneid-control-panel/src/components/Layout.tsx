import {
  Box,
  Button,
  CssBaseline,
  IconButton,
  styled,
  ThemeProvider,
  Toolbar,
  Typography,
} from '@mui/material';
import { ENV } from '../utils/env';
import { useAuth } from 'react-oidc-context';
import { theme } from '@pagopa/mui-italia';
import PersistentDrawerLeft from './Drawer';
import MenuIcon from '@mui/icons-material/Menu';
import { useState } from 'react';
import MuiAppBar, { AppBarProps as MuiAppBarProps } from '@mui/material/AppBar';
import * as Storage from '../utils/storage';
import { sessionStorageClientIdKey } from '../utils/constants';

const drawerWidth = 240;

type AppBarProps = {
  open?: boolean;
} & MuiAppBarProps;

type Props = {
  children: React.ReactNode;
};

const Layout = ({ children }: Props) => {
  const { removeUser, signoutRedirect } = useAuth();
  const [open, setOpen] = useState(false);

  const handleDrawerOpen = () => {
    setOpen(true);
  };

  const handleDrawerClose = () => {
    setOpen(false);
  };

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

  const AppBar = styled(MuiAppBar, {
    shouldForwardProp: (prop) => prop !== 'open',
  })<AppBarProps>(({ theme, open }) => ({
    transition: theme.transitions.create(['margin', 'width'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    ...(open && {
      width: `calc(100% - ${drawerWidth}px)`,
      marginLeft: `${drawerWidth}px`,
      transition: theme.transitions.create(['margin', 'width'], {
        easing: theme.transitions.easing.easeOut,
        duration: theme.transitions.duration.enteringScreen,
      }),
    }),
  }));

  return (
    <ThemeProvider theme={theme}>
      <Box
        bgcolor={'#F5F5F5'}
        margin={0}
        sx={{
          display: 'flex',
          flexDirection: 'column',
          minHeight: '100vh',
        }}
      >
        <CssBaseline />
        <AppBar position="static" open={open}>
          <Toolbar>
            <IconButton
              aria-label="open drawer"
              color="primary"
              onClick={handleDrawerOpen}
              edge="start"
              sx={[
                {
                  mr: 2,
                  ':hover': { color: 'primary', backgroundColor: 'white' },
                },
                open && { display: 'none' },
              ]}
            >
              <MenuIcon />
            </IconButton>
            <Typography
              variant="h6"
              component="div"
              color={'white'}
              noWrap
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
        <PersistentDrawerLeft
          handleDrawerClose={handleDrawerClose}
          open={open}
          clientId={Storage.storageRead(sessionStorageClientIdKey, 'string')}
        />
        {children}
      </Box>
    </ThemeProvider>
  );
};

export default Layout;
