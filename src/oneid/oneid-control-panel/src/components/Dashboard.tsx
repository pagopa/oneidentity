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
  CircularProgress,
  Alert,
  Chip,
  FormHelperText,
} from '@mui/material';
import { Link, useParams } from 'react-router-dom';
import { SpidLevel, SamlAttribute, Client, ClientErrors } from '../types/api';
import { useAuth } from 'react-oidc-context';
import { useRegister } from '../hooks/useRegister';
import { FormArrayTextField } from './FormArrayTextField';
import { Notify } from './Notify';
import Layout from './Layout';

export const Dashboard = () => {
  const { user } = useAuth();
  const { client_id } = useParams(); // Get the client_id from the URL
  const [formData, setFormData] = useState<Partial<Client> | null>(null);
  const [errorUi, setErrorUi] = useState<ClientErrors | null>(null);
  const [notify, setNotify] = useState<Notify>({ open: false });

  const {
    clientQuery: {
      data: fetchedClientData,
      isLoading: isLoadingClient,
      error: fetchError,
    },
    createOrUpdateClientMutation: {
      data: clientUpdated,
      mutate: createOrUpdateClient,
      error: updateError,
      isPending: isUpdating,
    },
  } = useRegister(client_id);

  useEffect(() => {
    if (fetchedClientData) {
      setFormData({ ...fetchedClientData, client_id });
    }
  }, [client_id, fetchedClientData]);

  useEffect(() => {
    if (updateError) {
      console.error('Error updating client:', updateError);
      setErrorUi(updateError as unknown as ClientErrors);
      setNotify({
        open: true,
        message: 'Error updating client',
        severity: 'error',
      });
    }
    if (clientUpdated) {
      console.log('Client updated successfully:', clientUpdated);
      setErrorUi(null);
      setNotify({
        open: true,
        message: 'Client updated successfully',
        severity: 'success',
      });
    }
  }, [updateError, clientUpdated]);

  const isFormValid = () => {
    return (
      !!formData?.client_name &&
      !!formData?.redirect_uris?.length &&
      !!formData?.default_acr_values?.length &&
      !!formData?.saml_requested_attributes?.length
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    console.log('Form submitted', formData);

    if (!formData && !isFormValid()) {
      console.error('Form is not valid');
    }

    createOrUpdateClient({
      data: formData as Omit<Client, 'client_id' | 'client_secret'>,
      clientId: client_id,
    });
  };

  const handleChange =
    (field: keyof Client) => (e: React.ChangeEvent<HTMLInputElement>) => {
      setFormData((prev) => ({ ...prev, [field]: e.target.value }));
    };

  if (isLoadingClient) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Layout>
      {fetchError && (
        <Box sx={{ mt: 4 }}>
          <Alert severity="error">
            {fetchError instanceof Error
              ? fetchError.message
              : 'An error occurred'}
          </Alert>
        </Box>
      )}
      <Typography variant="h6" sx={{ mt: 2, ml: 3 }}>
        User: {user?.profile?.email}
      </Typography>
      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{ p: 3, maxWidth: 800, mx: 'auto' }}
      >
        <Typography variant="h5" gutterBottom>
          Client Information
        </Typography>

        <TextField
          fullWidth
          label="Client ID"
          value={formData?.client_id || ''}
          disabled
          margin="normal"
        />

        <TextField
          fullWidth
          required
          label="Client Name"
          value={formData?.client_name || ''}
          onChange={handleChange('client_name')}
          margin="normal"
          error={!!(errorUi as ClientErrors)?.client_name?._errors}
          helperText={(errorUi as ClientErrors)?.client_name?._errors}
        />

        <TextField
          fullWidth
          label="Logo URI"
          value={formData?.logo_uri || ''}
          onChange={handleChange('logo_uri')}
          margin="normal"
          error={!!(errorUi as ClientErrors)?.logo_uri?._errors}
          helperText={(errorUi as ClientErrors)?.logo_uri?._errors}
        />

        <TextField
          fullWidth
          label="Policy URI"
          value={formData?.policy_uri || ''}
          onChange={handleChange('policy_uri')}
          margin="normal"
          error={!!(errorUi as ClientErrors)?.policy_uri?._errors}
          helperText={(errorUi as ClientErrors)?.policy_uri?._errors}
        />

        <TextField
          fullWidth
          label="Terms of Service URI"
          value={formData?.tos_uri || ''}
          onChange={handleChange('tos_uri')}
          margin="normal"
          error={!!(errorUi as ClientErrors)?.tos_uri?._errors}
          helperText={(errorUi as ClientErrors)?.tos_uri?._errors}
        />

        <FormControl
          fullWidth
          margin="normal"
          required
          error={!!(errorUi as ClientErrors)?.redirect_uris?._errors}
        >
          <FormArrayTextField
            formData={formData}
            setFormData={setFormData}
            fieldName="redirect_uris"
            label="Redirect URIs"
            errors={errorUi as ClientErrors}
          />
        </FormControl>

        <FormControl
          fullWidth
          margin="normal"
          required
          error={!!(errorUi as ClientErrors)?.default_acr_values?._errors}
        >
          <InputLabel id="spid-level-label">SPID Level</InputLabel>
          <Select
            labelId="spid-level-label"
            id="spid-level-select"
            multiple
            value={formData?.default_acr_values || []}
            renderValue={(selected) => (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {selected.map((value) => (
                  <Chip
                    key={value}
                    label={value.replace(
                      'https://www.spid.gov.it/Spid',
                      'Level '
                    )}
                  />
                ))}
              </Box>
            )}
            onChange={(e) =>
              setFormData((prev) => ({
                ...prev,
                default_acr_values: e.target.value as Array<SpidLevel>,
              }))
            }
            input={<OutlinedInput label={'SPID Level'} />}
            data-testid="spid-level-select"
          >
            {Object.values(SpidLevel).map((level) => (
              <MenuItem key={level} value={level}>
                {level.replace('https://www.spid.gov.it/Spid', 'Level ')}
              </MenuItem>
            ))}
          </Select>
          <FormHelperText>
            {(errorUi as ClientErrors)?.default_acr_values?._errors}
          </FormHelperText>
        </FormControl>

        <FormControl
          fullWidth
          margin="normal"
          required
          error={
            !!(errorUi as ClientErrors)?.saml_requested_attributes?._errors
          }
        >
          <InputLabel id="saml-attributes-label">SAML Attributes</InputLabel>
          <Select
            labelId="saml-attributes-label"
            id="saml-attributes-select"
            multiple
            value={formData?.saml_requested_attributes || []}
            onChange={(e) =>
              setFormData((prev) => ({
                ...prev,
                saml_requested_attributes: e.target
                  .value as Array<SamlAttribute>,
              }))
            }
            input={<OutlinedInput label="SAML Attributes" />}
            data-testid="saml-attributes-select"
          >
            {Object.values(SamlAttribute).map((attr) => (
              <MenuItem key={attr} value={attr}>
                {attr}
              </MenuItem>
            ))}
          </Select>
          <FormHelperText>
            {(errorUi as ClientErrors)?.saml_requested_attributes?._errors}
          </FormHelperText>
          <FormHelperText>
            Lista completa:{' '}
            <Link
              target="_blank"
              rel="noopener noreferrer"
              to={
                'https://docs.italia.it/italia/spid/spid-regole-tecniche/it/stabile/attributi.html'
              }
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
          disabled={isUpdating || !isFormValid()}
        >
          {isUpdating ? 'Saving...' : 'Save Changes'}
        </Button>
      </Box>

      <Notify
        open={notify.open}
        message={notify.message}
        severity={notify.severity}
        handleOpen={(open) => setNotify({ ...notify, open })}
      />
    </Layout>
  );
};
