import { useEffect, useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  OutlinedInput,
  FormHelperText,
} from '@mui/material';
import { Link, useLocation, useParams } from 'react-router-dom';
import { SamlAttribute, IdpUser, UserErrors } from '../../../types/api';
import { useAuth } from 'react-oidc-context';
import { Notify } from '../../../components/Notify';
import { useClient } from '../../../hooks/useClient';
import { every, fromPairs, map } from 'lodash';

export const AddOrUpdateUser = () => {
  const { user } = useAuth();
  const location = useLocation();
  const userToEdit = location.state?.userToEdit as IdpUser | undefined;
  const { id: usernameQueryParam } = useParams();
  const isEditMode = !!usernameQueryParam;
  const [formData, setFormData] = useState<Partial<IdpUser>>({});
  const [errorUi, setErrorUi] = useState<UserErrors | null>(null);
  const [notify, setNotify] = useState<Notify>({ open: false });

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
      setNotify({
        open: true,
        message: 'Utente creato con successo!',
        severity: 'success',
      });
      setFormData({});
    }
    if (isUserUpdated) {
      setNotify({
        open: true,
        message: 'Utente aggiornato con successo!',
        severity: 'success',
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
          User Information
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
          label="Password"
          value={formData?.password || ''}
          onChange={handleChange('password')}
          margin="normal"
          error={!!(errorUi as UserErrors)?.password?._errors}
          helperText={(errorUi as UserErrors)?.password?._errors}
          disabled={isEditMode}
        />

        <FormControl
          fullWidth
          margin="normal"
          required
          error={!!(errorUi as UserErrors)?.samlAttributes?._errors}
        >
          <InputLabel id="saml-attributes-label">SAML Attributes</InputLabel>
          <Select
            labelId="saml-attributes-label"
            id="saml-attributes-select"
            multiple
            value={Object.keys(formData?.samlAttributes || {})}
            onChange={(e) => {
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
            input={<OutlinedInput label="SAML Attributes" />}
            data-testid="saml-attributes-select"
          >
            {map(SamlAttribute, (attr) => (
              <MenuItem key={attr} value={attr}>
                {attr}
              </MenuItem>
            ))}
          </Select>

          {Object.entries(formData?.samlAttributes || {}).map(
            ([attribute, value]) => (
              <TextField
                key={attribute}
                label={`Valore per ${attribute}`}
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

          <FormHelperText>
            {(errorUi as UserErrors)?.samlAttributes?._errors}
          </FormHelperText>
          <FormHelperText>
            Lista completa:{' '}
            <Link
              target="_blank"
              rel="noopener noreferrer"
              to="https://docs.italia.it/italia/spid/spid-regole-tecniche/it/stabile/attributi.html"
            >
              Qui
            </Link>
          </FormHelperText>
        </FormControl>

        <Button
          type="submit"
          variant="contained"
          sx={{ mt: 2 }}
          data-testid="submit-button"
          disabled={!isFormValid()}
        >
          {'Save Changes'}
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
