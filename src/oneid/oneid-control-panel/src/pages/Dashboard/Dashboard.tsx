import { useEffect, useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  Select,
  MenuItem,
  FormControl,
  FormGroup,
  InputLabel,
  OutlinedInput,
  CircularProgress,
  Alert,
  Chip,
  FormHelperText,
  FormControlLabel,
  Switch,
} from '@mui/material';
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
import { SecretModal } from '../../components/SecretModal';
import { useModalManager } from '../../hooks/useModal';
import { ROUTE_PATH, sessionStorageClientIdKey } from '../../utils/constants';
import SamlAttributesSelectInput from '../../components/SamlAttributesSelectInput';
import * as Storage from '../../utils/storage';
import { isNil } from 'lodash';

export const Dashboard = () => {
  const { user } = useAuth();
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
      isSuccess: isUpdated,
    },
  } = useRegister();

  useEffect(() => {
    if (isUpdatePhase && fetchedClientData) {
      setFormData({
        ...fetchedClientData,
        clientId: fetchedClientData.clientId,
      });

      if (
        fetchedClientData.clientId &&
        fetchedClientData.clientId !==
          Storage.storageRead(sessionStorageClientIdKey, 'string')
      ) {
        Storage.storageWrite(
          sessionStorageClientIdKey,
          fetchedClientData.clientId,
          'string'
        );
      }
    }
  }, [isUpdatePhase, fetchedClientData]);

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
    if (isUpdated) {
      setErrorUi(null);
      const message = isUpdatePhase
        ? 'Client updated successfully, id: ' +
          Storage.storageRead(sessionStorageClientIdKey, 'string')
        : clientUpdated
          ? 'Client created successfully, id: ' + clientUpdated.clientId
          : 'Client created successfully, id: unknown';
      setNotify({
        open: true,
        message: message,
        severity: 'success',
      });

      // Save client id retrieved from api to session storage
      if (!isUpdatePhase) {
        const clientId = clientUpdated?.clientId;
        if (!isNil(clientId) && typeof clientId === 'string') {
          Storage.storageWrite(sessionStorageClientIdKey, clientId, 'string');
        } else {
          console.error('clientId is not a valid value:', clientId);
        }
      }
      // before redirecting we need to show a modal with clientId and clientSecret
      // open only if it is in creation phase, not an update
      if (!isUpdatePhase) {
        openModal('secretViewer');
      }
    }
  }, [updateError, isUpdated, clientUpdated, isUpdatePhase, openModal]);

  const isFormValid = () => {
    // TODO: if clientSchema inside api.ts is adjusted to reflect the actual optional and required fields, we can use:
    // const isFormValid = () => clientSchema.safeParse(formData).success;
    return (
      !!formData?.clientName &&
      !!formData?.redirectUris?.length &&
      !!formData?.defaultAcrValues?.length &&
      !!formData?.samlRequestedAttributes?.length
    );
  };

  const handleCloseSecretModal = () => {
    // TODO check cognito status before redirecting
    closeModal(() => {
      window.location.assign(`${ROUTE_PATH.DASHBOARD}`);
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData && !isFormValid()) {
      console.error('Form is not valid');
    } else {
      createOrUpdateClient({
        data: formData as Omit<Client, 'clientId' | 'clientSecret'>,
        clientId: Storage.storageRead(sessionStorageClientIdKey, 'string'),
      });
    }
  };

  const handleChange =
    (field: keyof Client) => (e: React.ChangeEvent<HTMLInputElement>) => {
      const { value, type, checked } = e.target;
      setFormData((prev) => ({
        ...prev,
        [field]: type === 'checkbox' ? checked : value,
      }));
    };

  if (isLoadingClient) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ bgcolor: 'grey.50', minHeight: '100vh' }}>
      {/* if client not found show creation page and don't show error alert */}
      {fetchError && fetchError.message !== 'Client not found' && (
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
          clientId:
            typeof clientUpdated?.clientId === 'string'
              ? clientUpdated.clientId
              : 'error',
          clientSecret:
            typeof clientUpdated?.clientSecret === 'string'
              ? clientUpdated.clientSecret
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
          value={formData?.clientId || ''}
          disabled
          margin="normal"
        />

        <TextField
          fullWidth
          required
          label="Client Name"
          value={formData?.clientName || ''}
          onChange={handleChange('clientName')}
          margin="normal"
          error={!!(errorUi as ClientErrors)?.clientName?._errors}
          helperText={(errorUi as ClientErrors)?.clientName?._errors}
        />

        <TextField
          fullWidth
          label="Logo URI"
          value={formData?.logoUri || ''}
          onChange={handleChange('logoUri')}
          margin="normal"
          error={!!(errorUi as ClientErrors)?.logoUri?._errors}
          helperText={(errorUi as ClientErrors)?.logoUri?._errors}
        />

        <TextField
          fullWidth
          label="Policy URI"
          value={formData?.policyUri || ''}
          onChange={handleChange('policyUri')}
          margin="normal"
          error={!!(errorUi as ClientErrors)?.policyUri?._errors}
          helperText={(errorUi as ClientErrors)?.policyUri?._errors}
        />

        <TextField
          fullWidth
          label="Terms of Service URI"
          value={formData?.tosUri || ''}
          onChange={handleChange('tosUri')}
          margin="normal"
          error={!!(errorUi as ClientErrors)?.tosUri?._errors}
          helperText={(errorUi as ClientErrors)?.tosUri?._errors}
        />

        <FormControl
          fullWidth
          margin="normal"
          required
          error={!!(errorUi as ClientErrors)?.redirectUris?._errors}
        >
          <FormArrayTextField
            formData={formData}
            setFormData={setFormData}
            fieldName="redirectUris"
            label="Redirect URIs"
            errors={errorUi as ClientErrors}
          />
        </FormControl>

        <FormControl
          fullWidth
          margin="normal"
          required
          error={!!(errorUi as ClientErrors)?.defaultAcrValues?._errors}
        >
          <InputLabel id="spid-level-label">SPID Level</InputLabel>
          <Select
            labelId="spid-level-label"
            id="spid-level-select"
            multiple
            value={formData?.defaultAcrValues || []}
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
                defaultAcrValues: e.target.value as Array<SpidLevel>,
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
            {(errorUi as ClientErrors)?.defaultAcrValues?._errors}
          </FormHelperText>
        </FormControl>

        <SamlAttributesSelectInput
          attributeSelectValues={formData?.samlRequestedAttributes}
          onChangeFunction={(e) =>
            setFormData((prev) => ({
              ...prev,
              samlRequestedAttributes: e.target.value as Array<SamlAttribute>,
            }))
          }
          errorHelperText={
            (errorUi as ClientErrors)?.samlRequestedAttributes?._errors
          }
        />

        <FormGroup sx={{ mt: 2, mb: 1 }}>
          <FormControlLabel
            control={
              <Switch
                sx={{ mr: 2, ml: 1 }}
                name="requiredSameIdp"
                checked={formData?.requiredSameIdp || false}
                onChange={handleChange('requiredSameIdp')}
              />
            }
            label="Required Same IDP"
          />
        </FormGroup>

        <Button
          type="submit"
          variant="contained"
          sx={{ mt: 5 }}
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
    </Box>
  );
};
