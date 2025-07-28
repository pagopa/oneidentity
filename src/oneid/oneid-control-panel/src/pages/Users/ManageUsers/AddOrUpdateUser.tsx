import { useEffect, useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  InputAdornment,
  IconButton,
} from '@mui/material';
import { useLocation, useParams, useNavigate } from 'react-router-dom';
import { SamlAttribute, IdpUser, UserErrors } from '../../../types/api';
import { useAuth } from 'react-oidc-context';
import { Notify } from '../../../components/Notify';
import { useClient } from '../../../hooks/useClient';
import { every, fromPairs, isEmpty } from 'lodash';
import { ROUTE_PATH } from '../../../utils/constants';
import SamlAttributesSelectInput from '../../../components/SamlAttributesSelectInput';
import { Visibility, VisibilityOff } from '@mui/icons-material';

export const AddOrUpdateUser = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const userToEdit = location.state?.userToEdit as IdpUser | undefined;
  const { id: usernameQueryParam } = useParams();
  const isEditMode = !!usernameQueryParam;
  const [formData, setFormData] = useState<Partial<IdpUser>>({});
  const [errorUi, setErrorUi] = useState<UserErrors | null>(null);
  const [notify, setNotify] = useState<Notify>({ open: false });
  const [showPassword, setShowPassword] = useState(false);

  const {
    createClientUsersMutation: {
      mutate: createClientUsersMutation,
      error: addClientUsersError,
      isSuccess: isUserCreated,
      isPending: isCreatingUser,
    },
    updateClientUsersMutation: {
      mutate: updateClientUsersMutation,
      error: updateClientUsersError,
      isSuccess: isUserUpdated,
      isPending: isUpdatingUser,
    },
  } = useClient();

  useEffect(() => {
    if (isEditMode && userToEdit) {
      setFormData(userToEdit);
    }
  }, [isEditMode, userToEdit]);

  useEffect(() => {
    if (addClientUsersError) {
      console.error('Error adding user:', addClientUsersError);
      setErrorUi(addClientUsersError as unknown as UserErrors);
      setNotify({
        open: true,
        message: 'Error adding user',
        severity: 'error',
      });
    }
    if (updateClientUsersError) {
      console.error('Error update user:', updateClientUsersError);
      setErrorUi(updateClientUsersError as unknown as UserErrors);
      setNotify({
        open: true,
        message: 'Error update user',
        severity: 'error',
      });
    }
  }, [addClientUsersError, updateClientUsersError]);

  useEffect(() => {
    if (isUserCreated) {
      const notifyCreation = {
        open: true,
        message: 'User Created!',
        severity: 'success',
      };
      setFormData({});
      navigate(ROUTE_PATH.USER_LIST, {
        state: { refresh: true, notify: notifyCreation },
      });
    }
    if (isUserUpdated) {
      const notifyUpdate = {
        open: true,
        message: 'User updated!',
        severity: 'success',
      };
      navigate(ROUTE_PATH.USER_LIST, {
        state: { refresh: true, notify: notifyUpdate },
      });
    }
  }, [isUserCreated, isUserUpdated]);

  const isFormValid = () => {
    return (
      !!formData?.username &&
      !!formData?.password &&
      !!formData?.samlAttributes &&
      every(formData.samlAttributes, (v) => v?.trim() !== '')
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData || !isFormValid()) {
      console.error('Form is not valid');
      return;
    }
    if (isEditMode) {
      updateClientUsersMutation({
        data: formData as IdpUser,
        username: formData.username as string,
      });
    } else {
      createClientUsersMutation({
        data: formData as IdpUser,
      });
    }
  };

  const handleChange =
    (field: keyof IdpUser) => (e: React.ChangeEvent<HTMLInputElement>) => {
      setFormData((prev) => ({ ...prev, [field]: e.target.value }));
    };

  return (
    <Box sx={{ bgcolor: 'grey.50', minHeight: '100vh' }}>
      <Typography variant="h6" sx={{ mt: 2, ml: 3 }}>
        User: {user?.profile?.email}
      </Typography>

      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{ p: 3, maxWidth: 800, mx: 'auto' }}
      >
        <Typography variant="h5" gutterBottom>
          User data
        </Typography>

        <TextField
          required
          fullWidth
          label="Username"
          value={formData?.username || ''}
          margin="normal"
          onChange={handleChange('username')}
          disabled={isEditMode}
        />

        <TextField
          fullWidth
          required
          type={showPassword ? 'text' : 'password'}
          label="Password"
          value={formData?.password || ''}
          onChange={handleChange('password')}
          margin="normal"
          error={!!(errorUi as UserErrors)?.password?._errors}
          helperText={(errorUi as UserErrors)?.password?._errors}
          disabled={isEditMode}
          InputProps={{
            ...(formData.password
              ? {
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={() => setShowPassword((prev) => !prev)}
                        edge="end"
                        size="small"
                      >
                        {showPassword ? (
                          <VisibilityOff
                            fontSize="small"
                            sx={{ color: 'grey' }}
                          />
                        ) : (
                          <Visibility fontSize="small" sx={{ color: 'grey' }} />
                        )}
                      </IconButton>
                    </InputAdornment>
                  ),
                }
              : {}),
          }}
        />

        <SamlAttributesSelectInput
          attributeSelectValues={
            Object.keys(formData?.samlAttributes || {}) as Array<SamlAttribute>
          }
          onChangeFunction={(e) => {
            const selected = e.target.value as Array<SamlAttribute>;
            // Record<String, String>
            const updated = fromPairs(
              selected.map((attr) => [
                attr,
                formData?.samlAttributes?.[attr] || '',
              ])
            );

            setFormData((prev) => ({
              ...prev,
              samlAttributes: updated,
            }));
          }}
          errorHelperText={(errorUi as UserErrors)?.samlAttributes?._errors}
        >
          <Box sx={{ mt: '8px' }}>
            {Object.entries(formData?.samlAttributes || {}).map(
              ([attribute, value]) => (
                <TextField
                  sx={{ mt: '16px', mb: '8px' }}
                  key={attribute}
                  label={`Value for ${attribute}`}
                  value={value}
                  onChange={(e) => {
                    const newValue = e.target.value;
                    setFormData((prev) => ({
                      ...prev,
                      samlAttributes: {
                        ...(prev?.samlAttributes || {}),
                        [attribute]: newValue,
                      },
                    }));
                  }}
                  margin="dense"
                  fullWidth
                />
              )
            )}
          </Box>
        </SamlAttributesSelectInput>

        <Button
          type="submit"
          variant="contained"
          sx={{ mt: 2 }}
          data-testid="submit-button"
          disabled={!isFormValid()}
        >
          {isEditMode ? 'Update User' : 'Add User'}
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
