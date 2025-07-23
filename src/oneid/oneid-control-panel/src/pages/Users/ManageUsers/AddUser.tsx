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
import { Link, useLocation } from 'react-router-dom';
import { SamlAttribute, UserApi, UserErrors } from '../../../types/api';
import { useAuth } from 'react-oidc-context';
import { Notify } from '../../../components/Notify';
import { useClient } from '../../../hooks/useClient';

//TODO if update user, pre-fill form with existing data
export const AddUser = () => {
  const { user } = useAuth();
  const location = useLocation();
  const userToEdit = location.state?.userToEdit as UserApi | undefined;
  const [formData, setFormData] = useState<Partial<UserApi>>({});
  const [errorUi, setErrorUi] = useState<UserErrors | null>(null);
  const [notify, setNotify] = useState<Notify>({ open: false });
  const [isUpdate, setIsUpdate] = useState<boolean>(false);

  const {
    createClientUsersMutation: {
      data: testUserCreated,
      mutate: createClientUsersMutation,
      error: addClientUsersError,
    },
    updateClientUsersMutation: {
      data: testUserUpdated,
      mutate: updateClientUsersMutation,
      error: updateClientUsersError,
    },
  } = useClient();

  useEffect(() => {
    if (userToEdit) {
      setFormData(userToEdit);
      setIsUpdate(true);
    }
  }, [userToEdit]);

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
    if (testUserCreated) {
      setNotify({
        open: true,
        message: 'Utente creato con successo!',
        severity: 'success',
      });
      setFormData({});
    }
    if (testUserUpdated) {
      setNotify({
        open: true,
        message: 'Utente aggiornato con successo!',
        severity: 'success',
      });
      setFormData({});
    }
  }, [testUserUpdated, testUserCreated]);

  const isFormValid = () => {
    return (
      !!formData?.username &&
      !!formData?.password &&
      !!formData?.samlAttributes &&
      Object.values(formData.samlAttributes).every((v) => v.trim() !== '')
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData || !isFormValid()) {
      console.error('Form is not valid');
      return;
    }
    if (isUpdate) {
      updateClientUsersMutation({
        data: formData as UserApi,
        username: formData.username as string,
      });
    } else {
      createClientUsersMutation({
        data: formData as UserApi,
      });
    }
  };

  const handleChange =
    (field: keyof UserApi) => (e: React.ChangeEvent<HTMLInputElement>) => {
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
        />

        <FormControl
          fullWidth
          margin="normal"
          required
          error={!!(errorUi as UserErrors)?.samlAttributes?._errors}
        >
          <InputLabel id="saml-attributes-label">
            SAML Attributes Users
          </InputLabel>
          <Select
            labelId="saml-attributes-label"
            id="saml-attributes-select"
            multiple
            value={Object.keys(formData?.samlAttributes || {})}
            onChange={(e) => {
              const selected = e.target.value as SamlAttribute[];
              const updated: Record<string, string> = {};
              selected.forEach((attr) => {
                updated[attr] = formData?.samlAttributes?.[attr] || '';
              });

              setFormData((prev) => ({
                ...prev,
                samlAttributes: updated,
              }));
            }}
            input={<OutlinedInput label="SAML Attributes" />}
            data-testid="saml-attributes-select"
          >
            {Object.values(SamlAttribute).map((attr) => (
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
