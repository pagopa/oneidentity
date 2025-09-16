import {
  Box,
  Divider,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import { Link, useLocation } from 'react-router-dom';
import { uniqueId } from 'lodash';
import { MoveToInbox, Mail, People } from '@mui/icons-material';
import { ROUTE_PATH, USER_ROOT_PATH } from '../utils/constants';
import { ENV } from '../utils/env';
import logoPagoPa from '../assets/logo_pagopa.png';

const dividerKey = 'divider';

const navData = (clientId?: string, isAuthenticated?: boolean) => [
  {
    name: 'Register',
    icon: <MoveToInbox fontSize="inherit" />,
    to: ROUTE_PATH.DASHBOARD,
    isVisible: true,
    matchPath: (pathname: string) => pathname.startsWith(ROUTE_PATH.DASHBOARD),
  },
  {
    name: 'Customize UI',
    icon: <Mail fontSize="inherit" />,
    to: `${ROUTE_PATH.CUSTOMIZE}/${clientId}`,
    isVisible: !!clientId && isAuthenticated,
    matchPath: (pathname: string) => pathname.startsWith(ROUTE_PATH.CUSTOMIZE),
  },
  {
    name: dividerKey,
  },
  {
    name: 'Manage Users',
    icon: <People fontSize="inherit" />,
    to: ROUTE_PATH.USER_LIST,
    isVisible: ENV.CURRENT_ENV !== 'prod',
    matchPath: (pathname: string) => pathname.startsWith(USER_ROOT_PATH),
  },
];

type DrawerNavLeft = {
  clientId?: string;
  appBarHeight: number;
  isAuthenticated?: boolean;
};

function DrawerNavLeft({
  clientId,
  appBarHeight,
  isAuthenticated,
}: DrawerNavLeft) {
  const location = useLocation();

  return (
    <Box
      sx={{
        height: '100%',
        maxWidth: 360,
        backgroundColor: 'background.paper',
      }}
    >
      <Box
        sx={{
          height: appBarHeight,
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          paddingTop: 1,
        }}
      >
        <img
          src={logoPagoPa}
          alt="Logo"
          style={{ maxWidth: '120px', height: 'auto' }}
        />
      </Box>
      <List component="nav">
        {navData(clientId, isAuthenticated).map((item) =>
          item.name === dividerKey ? (
            <Divider key={uniqueId()} />
          ) : (
            item.isVisible && (
              <ListItemButton
                key={item.name}
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                component={Link as any}
                to={item.to}
                selected={item.matchPath?.(location.pathname) ?? false}
              >
                <ListItemIcon>{item.icon}</ListItemIcon>
                <ListItemText primary={item.name} />
              </ListItemButton>
            )
          )
        )}
      </List>
    </Box>
  );
}

export default DrawerNavLeft;
