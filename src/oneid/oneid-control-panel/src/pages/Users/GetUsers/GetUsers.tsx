import { use, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Box, Button, Typography } from '@mui/material';
import { UserApi } from '../../../types/api';
import { useAuth } from 'react-oidc-context';
import { Notify } from '../../../components/Notify';
import UserTable from '../../../components/UserTable';
import { useClient } from '../../../hooks/useClient';
import { useQueryClient } from '@tanstack/react-query';

export const GetUser = () => {
  const { user } = useAuth();
  const userId = user?.profile.sub;
  const [users, setUsers] = useState<UserApi[]>([]);
  const navigate = useNavigate();
  const [notify, setNotify] = useState<Notify>({ open: false });

  //TODO onDelete & onUpdate

  const {
    getClientUsersList: { data, error: getClientUsersError },
    deleteClientUsersMutation: { mutateAsync: deleteClientUsersMutation },
  } = useClient();

  useEffect(() => {
    console.log(Array.isArray(data));
    if (data && 'users' in data && Array.isArray(data.users)) {
      setUsers(data.users);
    }

    if (getClientUsersError) {
      setNotify({
        open: true,
        message: 'Errore nel recupero degli utenti',
        severity: 'error',
      });
    }
  }, [data, getClientUsersError]);

  const handleEditUser = (user: UserApi) => {
    navigate('/dashboard/addUsers', { state: { userToEdit: user } });
  };
  const queryClient = useQueryClient();

  // Handler delete da passare alla tabella
  const handleDelete = async (username: string) => {
    if (window.confirm('Sei sicuro di voler eliminare questo utente?')) {
      try {
        await deleteClientUsersMutation({ username: username });
        setNotify({
          open: true,
          message: 'Utente eliminato con successo',
          severity: 'success',
        });
        queryClient.invalidateQueries({ queryKey: ['get_user_list', userId] });
      } catch (err) {
        setNotify({
          open: true,
          message: 'Errore durante l’eliminazione dell’utente',
          severity: 'error',
        });
      }
    }
  };

  const renderUserTable = () => {
    if (users.length !== 0) {
      return (
        <UserTable
          users={users}
          onDelete={(userId) => handleDelete(userId)}
          onEdit={(user) => handleEditUser(user)}
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

  return (
    <Box sx={{ bgcolor: 'grey.50', minHeight: '100vh' }}>
      <Typography variant="h6" sx={{ mt: 2, ml: 3 }}>
        User: {user?.profile?.email}
      </Typography>
      <Box sx={{ p: 3, maxWidth: 800, mx: 'auto' }}>
        <Typography variant="h5" gutterBottom>
          User TEST List
        </Typography>
        {renderUserTable()}

        <Button
          variant="contained"
          sx={{ mt: 2 }}
          data-testid="submit-button"
          onClick={() => navigate('/dashboard/addUsers')}
          // disabled={!isFormValid()}
        >
          {'Aggiungi nuovo utente'}
        </Button>
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
