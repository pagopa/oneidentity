import { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Alert, Backdrop, Box, Button, CircularProgress } from '@mui/material';
import { IdpUser } from '../../../types/api';
import { useAuth } from 'react-oidc-context';
import { Notify } from '../../../components/Notify';
import UserTable from '../../../components/UserTable';
import { useClient, USER_LIST_QKEY } from '../../../hooks/useClient';
import { useQueryClient } from '@tanstack/react-query';
import { ROUTE_PATH } from '../../../utils/constants';
import { isEmpty, isNil } from 'lodash';
import ConfirmDialog from '../../../components/ConfirmDialog';
import { useModalManager } from '../../../hooks/useModal';
import AddIcon from '@mui/icons-material/Add';
import { PageContainer } from '../../../components/PageContainer';

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
        message: 'Error retrieving users',
        severity: 'error',
      });
    }
  }, [data, getIdpUsersError]);

  const handleEditUser = useCallback(
    (user: IdpUser) => {
      navigate(`${ROUTE_PATH.USER}/${user.username}`, {
        state: { userToEdit: user },
      });
    },
    [navigate]
  );

  useEffect(() => {
    if (isUserDeleted) {
      setNotify({
        open: true,
        message: 'User successfully deleted',
        severity: 'success',
      });
      queryClient.invalidateQueries({ queryKey: [USER_LIST_QKEY, userId] });
    }
    if (deleteClientUsersError) {
      console.error('Error update user:', deleteClientUsersError);
      setNotify({
        open: true,
        message: 'Error deleting user',
        severity: 'error',
      });
    }
  }, [isUserDeleted, deleteClientUsersError, queryClient, userId]);

  // get from state: refresh and notify
  // if refresh is true reload user data
  // if notify is not null show notify
  useEffect(() => {
    const notifyFromState = location.state?.notify;
    if (notifyFromState) {
      setNotify(notifyFromState);
    }
    if (location.state?.refresh) {
      queryClient.invalidateQueries({ queryKey: [USER_LIST_QKEY, userId] });
    }
    window.history.replaceState({}, document.title);
  }, [location.state, queryClient, userId]);

  const handleDelete = useCallback(() => {
    if (userToDelete) {
      deleteClientUsersMutation({ username: userToDelete });
    }
    closeModal();
  }, [userToDelete, deleteClientUsersMutation, closeModal]);

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
    <PageContainer>
      {getIdpUsersError && (
        <Box sx={{ mb: 4 }}>
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
            startIcon={<AddIcon />}
            data-testid="submit-button"
            onClick={() => navigate(ROUTE_PATH.USER)}
          >
            {'Add User'}
          </Button>
        </>
      )}

      <Backdrop open={isDeletingUser}>
        <CircularProgress color="secondary" />
      </Backdrop>

      <Notify
        open={notify.open}
        message={notify.message}
        severity={notify.severity}
        handleOpen={(open) => setNotify({ ...notify, open })}
      />
    </PageContainer>
  );
};
