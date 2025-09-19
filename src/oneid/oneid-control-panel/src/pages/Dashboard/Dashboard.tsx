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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from '@mui/material';
import {
  SpidLevel,
  SamlAttribute,
  ClientErrors,
  ClientWithoutSensitiveData,
} from '../../types/api';
import { useRegister } from '../../hooks/useRegister';
import { FormArrayTextField } from '../../components/FormArrayTextField';
import { Notify } from '../../components/Notify';
import { SecretModal } from '../../components/SecretModal';
import { useModalManager } from '../../hooks/useModal';
import { ROUTE_PATH } from '../../utils/constants';
import SamlAttributesSelectInput from '../../components/SamlAttributesSelectInput';
import { isEqual, isNil } from 'lodash';
import { clientDataWithoutSensitiveData } from '../../utils/client';
import { useClientId } from '../../context/ClientIdContext';
import SaveIcon from '@mui/icons-material/Save';
import AddIcon from '@mui/icons-material/Add';
import { PageContainer } from '../../components/PageContainer';
import { ContentBox } from '../../components/ContentBox';

export const Dashboard = () => {
  const [formData, setFormData] =
    useState<Partial<ClientWithoutSensitiveData> | null>(null);
  const [errorUi, setErrorUi] = useState<ClientErrors | null>(null);
  const [notify, setNotify] = useState<Notify>({ open: false });
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const openConfirm = () => setIsConfirmOpen(true);
  const closeConfirm = () => setIsConfirmOpen(false);
  const { isModalOpen, openModal, closeModal } = useModalManager();

  const { setClientId, clientId } = useClientId();

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

  const [isCreating, setIsCreating] = useState(false);

  useEffect(() => {
    if (isUpdatePhase && fetchedClientData) {
      setFormData(clientDataWithoutSensitiveData(fetchedClientData));

      if (
        fetchedClientData.clientId &&
        fetchedClientData.clientId !== clientId
      ) {
        setClientId(fetchedClientData.clientId);
      }
    }
  }, [isUpdatePhase, fetchedClientData, clientId, setClientId]);

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
      const message = isCreating
        ? 'Client created successfully'
        : 'Client updated successfully';
      setNotify({
        open: true,
        message: message,
        severity: 'success',
      });

      // Save client id retrieved from api to session storage
      if (!isUpdatePhase) {
        const newClientId = clientUpdated?.clientId;
        if (!isNil(newClientId) && typeof newClientId === 'string') {
          setClientId(newClientId);
        } else {
          console.error('clientId is not a valid value:', newClientId);
        }
      }
      // before redirecting we need to show a modal with clientId and clientSecret
      // open only if it is in creation phase, not an update
      if (!isUpdatePhase) {
        openModal('secretViewer');
      }
    }
  }, [
    updateError,
    isUpdated,
    clientUpdated,
    isUpdatePhase,
    openModal,
    isCreating,
    setClientId,
  ]);

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

  const doSubmit = () => {
    if (!formData && !isFormValid()) {
      console.error('Form is not valid');
    } else {
      const existingClientId = fetchedClientData?.clientId || clientId;
      setIsCreating(!existingClientId);
      createOrUpdateClient({
        data: formData as ClientWithoutSensitiveData,
        clientId: clientId,
      });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    // open modal
    e.preventDefault();

    // open modal only if is update stauts && spid level or saml attributes are modified
    if (
      clientId &&
      (!isEqual(
        formData?.defaultAcrValues,
        fetchedClientData?.defaultAcrValues
      ) ||
        !isEqual(
          formData?.samlRequestedAttributes,
          fetchedClientData?.samlRequestedAttributes
        ))
    ) {
      openConfirm();
    } else {
      doSubmit();
    }
  };

  const handleConfirmSubmit = () => {
    // confirm changes submit request
    closeConfirm();
    doSubmit();
  };

  const handleChange =
    (field: keyof ClientWithoutSensitiveData) =>
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const { value, type, checked } = e.target;
      setFormData((prev) => ({
        ...prev,
        [field]:
          type === 'checkbox'
            ? checked
            : value?.trim() === '' // treat empty string as undefined (user can remove a field)
              ? undefined
              : value,
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
    <PageContainer>
      {fetchError && fetchError.message !== 'Client not found' && (
        <Box sx={{ mb: 4 }}>
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

      <Box component="form" onSubmit={handleSubmit}>
        <ContentBox>
          <Typography variant="h5" gutterBottom>
            Client Information
          </Typography>

          {clientId && (
            <TextField
              hidden
              fullWidth
              label="Client ID"
              value={clientId || ''}
              disabled
              margin="normal"
            />
          )}

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
        </ContentBox>

        <Box>
          <Button
            type="submit"
            variant="contained"
            startIcon={clientId ? <SaveIcon /> : <AddIcon />}
            sx={{ mt: 3 }}
            data-testid="submit-button"
            disabled={isUpdating || !isFormValid()}
          >
            {clientId
              ? isUpdating
                ? 'Saving...'
                : 'Save Changes'
              : isUpdating
                ? 'Creating...'
                : 'Create Client'}
          </Button>
        </Box>
      </Box>
      <Dialog
        open={isConfirmOpen}
        onClose={closeConfirm}
        aria-labelledby="confirm-submit-title"
      >
        <DialogTitle id="confirm-submit-title">Confirm changes</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Updating SPID Level and SAML Attributes fields may require
            re-sharing them with the SPID and CIE authorities (AgID and IPZS).
            Please confirm you want to proceed
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeConfirm} disabled={isUpdating}>
            Cancel
          </Button>
          <Button
            onClick={handleConfirmSubmit}
            variant="contained"
            autoFocus
            disabled={isUpdating}
          >
            {isUpdating ? 'Saving...' : 'Confirm'}
          </Button>
        </DialogActions>
      </Dialog>
      <Notify
        open={notify.open}
        message={notify.message}
        severity={notify.severity}
        handleOpen={(open) => setNotify({ ...notify, open })}
      />
    </PageContainer>
  );
};
