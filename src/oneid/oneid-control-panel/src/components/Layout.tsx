import { useState } from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import CssBaseline from '@mui/material/CssBaseline';
import Drawer from '@mui/material/Drawer';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import { ENV } from '../utils/env';
import { ButtonBase, Menu, MenuItem, ThemeProvider } from '@mui/material';
import DrawerNav from './DrawerNav';
import { theme } from '@pagopa/mui-italia';
import { useAuth } from 'react-oidc-context';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import { ArrowDropDown, Logout } from '@mui/icons-material';
import { useClientId } from '../context/ClientIdContext';
import { useLocation } from 'react-router-dom';
import { ROUTE_PATH } from '../utils/constants';

const drawerWidth = 240;
const appBarHeight = 64;

const SHOW_TOOLBAR_TITLE = true;

type Props = {
  children: React.ReactNode;
};

function Layout({ children }: Props) {
  const { removeUser, signoutRedirect, user, isAuthenticated } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);
  const { clientId } = useClientId();

  const location = useLocation();
  const isLoginPage = location.pathname === ROUTE_PATH.LOGIN;
  const appBarAndMainContentWidth = isLoginPage
    ? { sm: '100%' }
    : { sm: `calc(100% - ${drawerWidth}px)` };

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const [userMenuAnchorEl, setUserMenuAnchorEl] = useState<null | HTMLElement>(
    null
  );
  const handleOpenUserDxMenu = (event: React.MouseEvent<HTMLElement>) => {
    setUserMenuAnchorEl(event.currentTarget);
  };
  const handleCloseUserDxMenu = () => {
    setUserMenuAnchorEl(null);
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

  const drawer = (
    <div>
      <DrawerNav
        appBarHeight={64}
        clientId={clientId}
        isAuthenticated={isAuthenticated}
      />
    </div>
  );

  return (
    <ThemeProvider theme={theme}>
      <Box sx={{ display: 'flex' }}>
        <CssBaseline />
        <AppBar
          position="fixed"
          sx={{
            width: appBarAndMainContentWidth,
            ml: { sm: `${drawerWidth}px` },
            height: `${appBarHeight}px`,
            justifyContent: 'center',
          }}
        >
          <Toolbar data-testid="responsiveToolbar">
            <IconButton
              aria-label="open drawer"
              edge="start"
              onClick={handleDrawerToggle}
              sx={{
                mr: 2,
                backgroundColor: 'unset',
                display: { sm: 'none' },
              }}
            >
              <MenuIcon sx={{ color: 'white' }} />
            </IconButton>
            {SHOW_TOOLBAR_TITLE && (
              <Typography
                variant="h6"
                noWrap
                color={'white'}
                component="div"
                sx={{
                  ml: { xs: 0, sm: 2 },
                  flexGrow: 1,
                  fontSize: { xs: '17px!important', sm: '20px!important' },
                }}
              >
                {'OneIdentity Client Management'}
              </Typography>
            )}

            {user && (
              <Box sx={{ flexGrow: 0 }}>
                <ButtonBase
                  disableRipple
                  onClick={handleOpenUserDxMenu}
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: { xs: 0, sm: 1 },
                    p: 0,
                    transition: 'opacity 0.2s',
                    color: 'white',
                    '&:hover': {
                      opacity: 0.8,
                      backgroundColor: 'transparent',
                    },
                  }}
                >
                  <AccountCircleIcon />
                  <Typography
                    color="inherit"
                    variant="body2"
                    fontWeight={600}
                    sx={{
                      display: { xs: 'none', sm: 'block' },
                    }}
                  >
                    {user?.profile.email}
                  </Typography>
                  <ArrowDropDown />
                </ButtonBase>
                <Menu
                  sx={{ mt: '45px' }}
                  id="menu-appbar"
                  anchorEl={userMenuAnchorEl}
                  anchorOrigin={{
                    vertical: 'top',
                    horizontal: 'right',
                  }}
                  keepMounted
                  transformOrigin={{
                    vertical: 'top',
                    horizontal: 'right',
                  }}
                  open={Boolean(userMenuAnchorEl)}
                  onClose={handleCloseUserDxMenu}
                >
                  <Box
                    px={2}
                    py={1}
                    sx={{
                      display: { xs: 'block', sm: 'none' },
                    }}
                  >
                    <Typography variant="body2" color="text.secondary">
                      {user?.profile.email}
                    </Typography>
                  </Box>
                  <MenuItem onClick={handleLogout}>
                    <Logout fontSize="small" sx={{ mr: 1 }} /> Logout
                  </MenuItem>
                </Menu>
              </Box>
            )}
          </Toolbar>
        </AppBar>
        {!isLoginPage && (
          <Box
            component="nav"
            sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
          >
            <Drawer
              container={undefined}
              variant="temporary"
              open={mobileOpen}
              onClose={handleDrawerToggle}
              ModalProps={{
                keepMounted: true,
              }}
              sx={{
                display: { xs: 'block', sm: 'none' },
                '& .MuiDrawer-paper': {
                  boxSizing: 'border-box',
                  width: drawerWidth,
                },
              }}
            >
              {drawer}
            </Drawer>
            <Drawer
              variant="permanent"
              sx={{
                display: { xs: 'none', sm: 'block' },
                '& .MuiDrawer-paper': {
                  boxSizing: 'border-box',
                  width: drawerWidth,
                },
              }}
              open
            >
              {drawer}
            </Drawer>
          </Box>
        )}
        <Box
          bgcolor={'background.default'}
          component="main"
          margin={0}
          sx={{
            display: 'flex',
            flexGrow: 1,
            flexDirection: 'column',
            p: 3,
            width: appBarAndMainContentWidth,
            height: `calc(100vh - ${appBarHeight}px)`,
            marginTop: `${appBarHeight}px`,
            overflow: 'auto',
          }}
        >
          {children}
        </Box>
      </Box>
    </ThemeProvider>
  );
}

export default Layout;
