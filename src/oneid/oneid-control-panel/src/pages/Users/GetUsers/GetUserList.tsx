import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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

export const GetUserList = () => {
  const { user } = useAuth();
  const userId = user?.profile.sub;
  const [users, setUsers] = useState<Array<IdpUser>>([]);
  const navigate = useNavigate();
  const [notify, setNotify] = useState<Notify>({ open: false });
  const queryClient = useQueryClient();

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
    console.log(data);
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

  // Handler delete da passare alla tabella
  const handleDelete = async (username: string) => {
    if (window.confirm('Sei sicuro di voler eliminare questo utente?')) {
      deleteClientUsersMutation({ username: username });
    }
  };

  const renderUserTable = () => {
    if (!isEmpty(users)) {
      return (
        <UserTable
          users={users}
          onDelete={handleDelete}
          onEdit={handleEditUser}
        />
      );
    } else {
      return (
        <Alert severity="info">
          Non sono presenti utenti di test registrati.
        </Alert>
      );
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
            <Typography variant="h5" gutterBottom>
              User TEST List
            </Typography>
            {renderUserTable()}

            <Button
              variant="contained"
              sx={{ mt: 2 }}
              data-testid="submit-button"
              onClick={() => navigate(ROUTE_PATH.USER)}
            >
              {'Aggiungi nuovo utente'}
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
