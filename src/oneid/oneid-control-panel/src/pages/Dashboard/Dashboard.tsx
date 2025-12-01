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
  Link,
  Divider,
} from '@mui/material';
import {
  SpidLevel,
  SamlAttribute,
  ClientErrors,
  ClientWithoutSensitiveData,
  ValidatePlanSchema,
  ValidateError,
  PlanErrors,
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
import FieldWithInfo from '../../components/FieldWithInfo';
import { tooltipLinkSx } from '../../utils/styles';
import ConfirmDialog from '../../components/ConfirmDialog';

export const Dashboard = () => {
  const [formData, setFormData] =
    useState<Partial<ClientWithoutSensitiveData> | null>(null);
  const [pairWiseData, setPairWiseData] = useState<Partial<ValidatePlanSchema>>(
    {
      apiKeyId: '',
      apiKeyValue: '',
    }
  );
  const [errorUi, setErrorUi] = useState<ClientErrors | null>(null);
  const [notify, setNotify] = useState<Notify>({ open: false });
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const openConfirm = () => setIsConfirmOpen(true);
  const closeConfirm = () => setIsConfirmOpen(false);
  const [contentDialog, setContentDialog] = useState<string>('');
  const { isModalOpen, openModal, closeModal } = useModalManager();
  const enablingPairWiseDialogContent =
    'By enabling PairWise One Identity, it will use PDV to obtain the subject identifier.';
  const disablingPairWiseDialogContent =
    'Disabling PairWise pseudonymous will stop sharing communication from PDV Building Block and OI.';
  const metadataDialogContent =
    'Updating SPID Level and SAML Attributes fields may require re-sharing them with the SPID and CIE authorities (AgID and IPZS).';
  const confirmText = 'Please confirm you want to proceed';
  const { setClientId, clientId } = useClientId();

  const {
    clientQuery: {
      data: fetchedClientData,
      isLoading: isLoadingClient,
      error: fetchError,
      isSuccess: isUpdatePhase,
    },
    planQuery: {
      data: planList,
      isLoading: isLoadingPlanList,
      error: planListError,
      isSuccess: planListSuccess,
    },
    createOrUpdateClientMutation: {
      data: clientUpdated,
      mutate: createOrUpdateClient,
      error: updateError,
      isPending: isUpdating,
      isSuccess: isUpdated,
    },
    validatePlanKeyMutation: {
      data: validationResult,
      mutate: validatePlanKey,
      error: validateError,
      isPending: isValidating,
      isSuccess: isValidated,
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

  const validationIsValid = validationResult?.valid;

  useEffect(() => {
    if (isValidated) {
      if (validationIsValid) {
        setErrorUi(null);
        setNotify({
          open: true,
          message: 'PairWise key validated successfully',
          severity: 'success',
        });
      } else {
        setErrorUi(null);
        setNotify({
          open: true,
          message: 'PairWise key validation failed',
          severity: 'error',
        });
      }
    }
    if (validateError) {
      console.error('Error validatig api key:', validateError);
      setErrorUi(validateError as unknown as ValidateError);
      setNotify({
        open: true,
        message: 'PairWise error validating api key',
        severity: 'error',
      });
    }
    if (planListError) {
      setErrorUi(planListError as unknown as PlanErrors);
      setNotify({
        open: true,
        message: 'PairWise error retrieving plan list',
        severity: 'error',
      });
    }
  }, [validateError, isValidated, planListError, validationIsValid]);

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
      if (!isUpdatePhase && !updateError) {
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
        pairWiseData: pairWiseData as ValidatePlanSchema,
      });
    }
  };

  const createContentDialog = () => {
    const isPairwiseChanged = !isEqual(
      formData?.pairwise,
      fetchedClientData?.pairwise
    );

    const isMetadataChanged =
      !isEqual(
        formData?.samlRequestedAttributes,
        fetchedClientData?.samlRequestedAttributes
      ) ||
      !isEqual(formData?.defaultAcrValues, fetchedClientData?.defaultAcrValues);

    const pairWiseText =
      isPairwiseChanged &&
      (formData?.pairwise
        ? enablingPairWiseDialogContent
        : disablingPairWiseDialogContent);

    const changeType = `${isPairwiseChanged ? 'pairwise' : ''}${isPairwiseChanged && isMetadataChanged ? '+' : ''}${isMetadataChanged ? 'metadata' : ''}`;
    switch (changeType) {
      case 'pairwise+metadata':
        return `${metadataDialogContent}\n\n${pairWiseText} ${confirmText}`;
      case 'pairwise':
        return `${pairWiseText} ${confirmText}`;
      case 'metadata':
        return `${metadataDialogContent} ${confirmText}`;
      default:
        return '';
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    // open modal
    e.preventDefault();
    const content = createContentDialog();
    setContentDialog(content || '');
    // open modal only if is update stauts && spid level or saml attributes are modified
    if (clientId && content !== '') {
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

  if (isLoadingClient || isLoadingPlanList) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  const checkEnableSaveUpdateClientPairwiseBased = (): boolean =>
    !formData?.pairwise ||
    validationIsValid === true ||
    fetchedClientData?.pairwise === true;

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
            <FieldWithInfo
              tooltipText={
                <span>
                  Same IDP is a function that will return a custom request
                  indicating whether the user has logged in using the same IDP
                  as the previous time.
                  <br />
                  More info can be found{' '}
                  <Link
                    href="https://pagopa.atlassian.net/wiki/spaces/OI/pages/1560477700/RFC+OI-004+Check+last+used+IDP+-+OTP"
                    target="_blank"
                    rel="noopener noreferrer"
                    sx={tooltipLinkSx}
                  >
                    here
                  </Link>
                </span>
              }
              placement="top"
            >
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
            </FieldWithInfo>
          </FormGroup>

          <FormGroup sx={{ mt: 2, mb: 1 }}>
            <Divider sx={{ mb: 3 }} />
            <FieldWithInfo
              tooltipText={
                <span>
                  Pairwise pseudonymous is an OIDC object used to replace the
                  subject identifier. One Identity will leverage on PDV Building
                  block to calculate or obtain it.
                  <br />
                  More info can be found{' '}
                  <Link
                    href="https://pagopa.atlassian.net/wiki/spaces/OI/pages/2101936152/OI+-+Integrazione+PDV"
                    target="_blank"
                    rel="noopener noreferrer"
                    sx={tooltipLinkSx}
                  >
                    here
                  </Link>
                </span>
              }
              placement="top"
            >
              <FormControlLabel
                control={
                  <Switch
                    sx={{ mr: 2, ml: 1 }}
                    name="pairWise"
                    checked={formData?.pairwise || false}
                    onChange={handleChange('pairwise')}
                  />
                }
                label="Pairwise Enabled"
              />
            </FieldWithInfo>
          </FormGroup>
          {formData?.pairwise && (
            <Box
              sx={{
                display: 'flex',
                flexDirection: 'column',
                gap: 2,
                mt: 2,
                width: '100%',
              }}
            >
              {planListError && (
                <Box>
                  <Alert severity="error">
                    {planListError instanceof Error
                      ? planListError.message
                      : 'An error occurred while retrieving plan list'}
                  </Alert>
                </Box>
              )}
              {fetchedClientData?.pairwise && (
                <Box>
                  <Alert severity="info">
                    PairWise is already enabled for this client if you want to
                    change the configuration please disable it and use save
                    button, then enable it again and set new values.
                  </Alert>
                </Box>
              )}
              {!fetchedClientData?.pairwise && (
                <>
                  <FormControl fullWidth>
                    <InputLabel id="pairwise-select-label">
                      Plan Name
                    </InputLabel>
                    <Select
                      labelId="pairwise-select-label"
                      name="pairwiseOption"
                      value={pairWiseData?.apiKeyId || ''}
                      label="Plan Name"
                      onChange={(e) =>
                        setPairWiseData((prev) => ({
                          ...prev,
                          apiKeyId: e.target.value,
                        }))
                      }
                      renderValue={(selected) => {
                        const selectedPlan = planList?.api_keys?.find(
                          (plan) => plan.id === selected
                        );
                        return selectedPlan ? selectedPlan.name : '';
                      }}
                    >
                      {planListSuccess &&
                        planList?.api_keys.map((plan) => (
                          <MenuItem key={plan.id} value={plan.id}>
                            {plan.name}
                          </MenuItem>
                        ))}
                    </Select>
                  </FormControl>
                  <TextField
                    fullWidth
                    label="Key value"
                    name="pairwiseValue"
                    value={pairWiseData?.apiKeyValue || ''}
                    autoComplete="off"
                    onChange={(e) =>
                      setPairWiseData((prev) => ({
                        ...prev,
                        apiKeyValue: e.target.value,
                      }))
                    }
                  />

                  <Button
                    variant="contained"
                    disabled={
                      !pairWiseData?.apiKeyValue || !pairWiseData?.apiKeyId
                    }
                    onClick={() =>
                      validatePlanKey({
                        data: pairWiseData as ValidatePlanSchema,
                      })
                    }
                  >
                    {isValidating ? 'Validating…' : 'Validate'}
                  </Button>
                </>
              )}
              <Notify
                open={notify.open}
                message={notify.message}
                severity={notify.severity}
                handleOpen={(open) => setNotify({ ...notify, open })}
              />
            </Box>
          )}
        </ContentBox>

        <Box>
          <Button
            type="submit"
            variant="contained"
            startIcon={clientId ? <SaveIcon /> : <AddIcon />}
            sx={{ mt: 3 }}
            data-testid="submit-button"
            disabled={
              !checkEnableSaveUpdateClientPairwiseBased() ||
              isUpdating ||
              !isFormValid()
            }
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
      <ConfirmDialog
        open={isConfirmOpen}
        title="Confirm changes"
        content={contentDialog}
        onCancel={closeConfirm}
        onConfirm={handleConfirmSubmit}
        confirmText={isUpdating ? 'Saving...' : 'Confirm'}
        cancelText="Cancel"
        confirmButtonProps={{
          color: 'primary',
          variant: 'contained',
        }}
        cancelButtonProps={{
          color: 'primary',
        }}
      />
      <Notify
        open={notify.open}
        message={notify.message}
        severity={notify.severity}
        handleOpen={(open) => setNotify({ ...notify, open })}
      />
    </PageContainer>
  );
};
