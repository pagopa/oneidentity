import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Typography,
} from '@mui/material';
import { IdpUser } from '../../../types/api';
import { useAuth } from 'react-oidc-context';
import { Notify } from '../../../components/Notify';
import UserTable from '../../../components/UserTable';
import { useClient } from '../../../hooks/useClient';
import { useQueryClient } from '@tanstack/react-query';
import { ROUTE_PATH } from '../../../utils/constants';
import { isEmpty, isNil } from 'lodash';
import ConfirmDialog from '../../../components/ConfirmDialog';
import { useModalManager } from '../../../hooks/useModal';

export const GetUserList = () => {
  const { user } = useAuth();
  const userId = user?.profile.sub;
  const [users, setUsers] = useState<Array<IdpUser>>([]);
  const navigate = useNavigate();
  const location = useLocation();
  const [notify, setNotify] = useState<Notify>({ open: false });
  const queryClient = useQueryClient();
  const [userToDelete, setUserToDelete] = useState<string | null>(null);
  const { isModalOpen, openModal, closeModal } = useModalManager();

  const {
    getClientUsersList: { data, error: getIdpUsersError, isLoading, isSuccess },
    deleteClientUsersMutation: {
      mutate: deleteClientUsersMutation,
      error: deleteClientUsersError,
      isSuccess: isUserDeleted,
      isPending: isDeletingUser,
    },
  } = useClient();

  useEffect(() => {
    if (!isNil(data) && 'users' in data) {
      setUsers(data.users);
    }

    if (getIdpUsersError) {
      setNotify({
        open: true,
        message: 'Errore nel recupero degli utenti',
        severity: 'error',
      });
    }
  }, [data, getIdpUsersError]);

  const handleEditUser = (user: IdpUser) => {
    navigate(`${ROUTE_PATH.USER}/${user.username}`, {
      state: { userToEdit: user },
    });
  };

  useEffect(() => {
    if (isUserDeleted) {
      setNotify({
        open: true,
        message: 'Utente eliminato con successo',
        severity: 'success',
      });
      queryClient.invalidateQueries({ queryKey: ['get_user_list', userId] });
    }
    if (deleteClientUsersError) {
      console.error('Error update user:', deleteClientUsersError);
      setNotify({
        open: true,
        message: 'Errore durante l’eliminazione dell’utente',
        severity: 'error',
      });
    }
  }, [isUserDeleted, deleteClientUsersError, queryClient, userId]);

  useEffect(() => {
    if (location.state?.refresh) {
      queryClient.invalidateQueries({ queryKey: ['get_user_list', userId] });
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  // Handler delete da passare alla tabella
  const handleDelete = async () => {
    if (userToDelete) {
      deleteClientUsersMutation({ username: userToDelete });
    }
    closeModal();
  };

  const handleDeleteClick = (username: string) => {
    setUserToDelete(username);
    openModal('confirm');
  };

  const renderUserTable = () => {
    if (!isEmpty(users)) {
      return (
        <UserTable
          users={users}
          onDelete={handleDeleteClick}
          onEdit={handleEditUser}
        />
      );
    } else {
      return <Alert severity="info">There are no registered test users</Alert>;
    }
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 6 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ bgcolor: 'grey.50', minHeight: '100vh' }}>
      <Typography variant="h6" sx={{ mt: 2, ml: 3 }}>
        User: {user?.profile?.email}
      </Typography>
      <Box sx={{ p: 3, maxWidth: 800, mx: 'auto' }}>
        {getIdpUsersError && (
          <Box sx={{ mt: 4 }}>
            <Alert severity="error">
              {getIdpUsersError instanceof Error
                ? getIdpUsersError.message
                : 'An error occurred'}
            </Alert>
          </Box>
        )}
        {isSuccess && (
          <>
            {renderUserTable()}
            <ConfirmDialog
              open={isModalOpen('confirm')}
              title="Confirm deletion"
              content="Are you sure you want to delete this user?"
              onCancel={() => closeModal()}
              onConfirm={handleDelete}
              confirmText="Delete"
              cancelText="Cancel"
            />
            <Button
              variant="contained"
              sx={{ mt: 3 }}
              data-testid="submit-button"
              onClick={() => navigate(ROUTE_PATH.USER)}
            >
              {'Add user'}
            </Button>
          </>
        )}
      </Box>

      <Notify
        open={notify.open}
        message={notify.message}
        severity={notify.severity}
        handleOpen={(open) => setNotify({ ...notify, open })}
      />
    </Box>
  );
};
