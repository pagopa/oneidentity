import { styled, useTheme } from '@mui/material/styles';
import Drawer from '@mui/material/Drawer';
import List from '@mui/material/List';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import InboxIcon from '@mui/icons-material/MoveToInbox';
import MailIcon from '@mui/icons-material/Mail';
import { People } from '@mui/icons-material';
import { Link } from 'react-router-dom';
import { ROUTE_PATH } from '../utils/constants';
import { ENV } from '../utils/env';

const drawerWidth = 240;

const DrawerHeader = styled('div')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  padding: theme.spacing(0, 1),
  // necessary for content to be below app bar
  ...theme.mixins.toolbar,
  justifyContent: 'flex-end',
}));
type PersistentDrawerLeftProps = {
  handleDrawerClose: () => void;
  open: boolean;
  clientId?: string;
};

export default function PersistentDrawerLeft({
  handleDrawerClose,
  open,
  clientId,
}: PersistentDrawerLeftProps) {
  const theme = useTheme();

  return (
    <Drawer
      sx={{
        width: drawerWidth,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: drawerWidth,
          boxSizing: 'border-box',
        },
      }}
      variant="persistent"
      anchor="left"
      open={open}
    >
      <DrawerHeader>
        <IconButton onClick={handleDrawerClose}>
          {theme.direction === 'ltr' ? (
            <ChevronLeftIcon />
          ) : (
            <ChevronRightIcon />
          )}
        </IconButton>
      </DrawerHeader>
      <Divider />
      <List>
        <ListItem disablePadding component={Link} to={ROUTE_PATH.DASHBOARD}>
          <ListItemButton>
            <ListItemIcon>
              <InboxIcon />
            </ListItemIcon>
            <ListItemText primary={'Register'} />
          </ListItemButton>
        </ListItem>
        {clientId && (
          <ListItem
            disablePadding
            component={Link}
            to={`${ROUTE_PATH.CUSTOMIZE}/${clientId}`}
          >
            <ListItemButton>
              <ListItemIcon>
                <MailIcon />
              </ListItemIcon>
              <ListItemText primary={'Customize UI'} />
            </ListItemButton>
          </ListItem>
        )}
      </List>
      {ENV.CURRENT_ENV !== 'prod' && (
        <>
          <Divider />
          <List>
            <ListItem
              disablePadding
              component={Link}
              to={`${ROUTE_PATH.USER_LIST}`}
            >
              <ListItemButton>
                <ListItemIcon>
                  <People />
                </ListItemIcon>
                <ListItemText primary={'Manage Users'} />
              </ListItemButton>
            </ListItem>
          </List>
        </>
      )}
    </Drawer>
  );
}
