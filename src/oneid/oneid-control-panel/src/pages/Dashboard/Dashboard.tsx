import { useCallback, useEffect, useState } from 'react';
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
import {
  SpidLevel,
  SamlAttribute,
  Client,
  ClientErrors,
} from '../../types/api';
import { useAuth } from 'react-oidc-context';
import { useRegister } from '../../hooks/useRegister';
import { FormArrayTextField } from '../../components/FormArrayTextField';
import { Notify } from '../../components/Notify';
import Layout from '../../components/Layout';
import { useClient } from '../../hooks/useClient';
import { SecretModal } from '../../components/SecretModal';
import { useModalManager } from '../../hooks/useModal';

export const Dashboard = () => {
  const { user } = useAuth();
  const { client_id } = useParams(); // Get the client_id from the URL
  const [formData, setFormData] = useState<Partial<Client> | null>(null);
  const [errorUi, setErrorUi] = useState<ClientErrors | null>(null);
  const [notify, setNotify] = useState<Notify>({ open: false });
  const { isModalOpen, openModal, closeModal } = useModalManager();

  const {
    clientQuery: {
      data: fetchedClientData,
      isLoading: isLoadingClient,
      error: fetchError,
      isSuccess: isUpdatePhase,
    },
    createOrUpdateClientMutation: {
      data: clientUpdated,
      mutate: createOrUpdateClient,
      error: updateError,
      isPending: isUpdating,
    },
  } = useRegister(client_id);

  const {
    setCognitoProfile: {
      data: cognitoUpdated,
      mutate: setCognitoProfile,
      error: cognitoError,
    },
  } = useClient();

  useEffect(() => {
    if (fetchedClientData) {
      setFormData({ ...fetchedClientData, client_id });
    }
  }, [client_id, fetchedClientData]);

  const updateCognitoMapping = useCallback(() => {
    if (
      clientUpdated?.client_id &&
      typeof clientUpdated.client_id === 'string' &&
      user?.profile.sub &&
      user?.id_token
    ) {
      setCognitoProfile({
        clientId: clientUpdated.client_id,
      });
    }
  }, [clientUpdated, setCognitoProfile, user?.id_token, user?.profile.sub]);

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
      setErrorUi(null);
      setNotify({
        open: true,
        message: 'Client updated successfully, id: ' + clientUpdated.client_id,
        severity: 'success',
      });

      // Associate the client with the user in Cognito if not in update phase
      if (!isUpdatePhase) {
        updateCognitoMapping();
      }
      // before redirecting we need to show a modal with client_id and client_secret
      // open only if it is in creation phase, not an update
      if (!isUpdatePhase) {
        openModal('secretViewer');
      }
    }
  }, [
    updateError,
    clientUpdated,
    updateCognitoMapping,
    isUpdatePhase,
    openModal,
  ]);

  useEffect(() => {
    // If everything is ok, redirect to the dashboard's client in edit mode
    if (cognitoUpdated) {
      setErrorUi(null);
      setNotify({
        open: true,
        message:
          'Cognito updated successfully, id: ' + clientUpdated?.client_id,
        severity: 'success',
      });
    }

    if (cognitoError) {
      setNotify({
        open: true,
        message: 'Error updating cognito',
        severity: 'error',
      });
    }
  }, [clientUpdated?.client_id, cognitoError, cognitoUpdated, openModal]);

  const isFormValid = () => {
    return (
      !!formData?.client_name &&
      !!formData?.redirect_uris?.length &&
      !!formData?.default_acr_values?.length &&
      !!formData?.saml_requested_attributes?.length
    );
  };

  const handleCloseSecretModal = () => {
    // TODO check cognito status before redirecting
    closeModal(() => {
      window.location.assign(`/dashboard/${clientUpdated?.client_id}`);
    });
  };
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

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

      <SecretModal
        title="Secret Viewer"
        onClose={handleCloseSecretModal}
        open={isModalOpen('secretViewer')}
        data={{
          client_id:
            typeof clientUpdated?.client_id === 'string'
              ? clientUpdated.client_id
              : 'error',
          client_secret:
            typeof clientUpdated?.client_secret === 'string'
              ? clientUpdated.client_secret
              : 'error',
        }}
      />

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
          hidden
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
