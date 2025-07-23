import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Button, Typography } from '@mui/material';
import { UserApi } from '../../../types/api';
import { useAuth } from 'react-oidc-context';
import { Notify } from '../../../components/Notify';
import UserTable from '../../../components/UserTable';

export const GetUser = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [notify, setNotify] = useState<Notify>({ open: false });

  //TODO add getUser request and manage response
  //TODO onDelete & onUpdate

  const mockUsers: UserApi[] = [
    {
      username: 'username1',
      password: 'password1',
      user_id: 'u1',
      samlAttributes: {
        email: 'email1',
        name: 'name1',
      },
    },
    {
      username: 'username2',
      password: 'password2',
      user_id: 'u2',
      samlAttributes: {
        email: 'email2',
        name: 'name2',
        fiscalNumber: 'fiscalCode2',
      },
    },
  ];

  const handleEditUser = (user: UserApi) => {
    navigate('/dashboard/addUsers', { state: { userToEdit: user } });
  };

  const handleDelete = async (userId: string) => {
    try {
      console.log('Deleting user with ID:', userId);
      setNotify({
        open: true,
        message: 'Utente eliminato con successo',
        severity: 'success',
      });
    } catch (err) {
      setNotify({
        open: true,
        message: 'Errore durante l’eliminazione dell’utente',
        severity: 'error',
      });
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

        <UserTable
          users={mockUsers}
          onDelete={(userId) => handleDelete(userId)}
          onEdit={(user) => handleEditUser(user)}
        />

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
