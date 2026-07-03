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
import {
  SpidLevel,
  SamlAttribute,
  SamlBinding,
  EidasAttributeSet,
  ClientErrors,
  ClientWithoutSensitiveData,
  ValidatePlanSchema,
  ValidateError,
  PlanErrors,
} from '../../types/api';
import { useRegister } from '../../hooks/useRegister';
import { FormArrayTextField } from '../../components/FormArrayTextField';
import { SecretModal } from '../../components/SecretModal';
import { useModalManager } from '../../hooks/useModal';
import { useNotification } from '../../context/NotificationContext';
import { ROUTE_PATH } from '../../utils/constants';
import SamlAttributesSelectInput from '../../components/SamlAttributesSelectInput';
import { isEqual, isNil } from 'lodash';
import { clientDataWithoutSensitiveData } from '../../utils/client';
import { useClientId } from '../../context/ClientIdContext';
import SaveIcon from '@mui/icons-material/Save';
import AddIcon from '@mui/icons-material/Add';
import { PageContainer } from '../../components/PageContainer';
import { ContentBox } from '../../components/ContentBox';
import ToggleSection from '../../components/ToggleSection';
import TooltipContentWithLink from '../../components/TooltipContent';
import ConfirmDialog from '../../components/ConfirmDialog';

const defaultFormData: Partial<ClientWithoutSensitiveData> = {
  samlBinding: SamlBinding.HTTP_POST,
};

const EIDAS_ATTRIBUTE_SET_OPTIONS = [
  {
    value: EidasAttributeSet.MINIMUM,
    label: 'Minimum set of attributes',
    description: 'spidCode, name, familyName, dateOfBirth',
  },
  {
    value: EidasAttributeSet.COMPLETE,
    label: 'Complete set of attributes',
    description:
      'spidCode, name, familyName, dateOfBirth, placeOfBirth, address, gender',
  },
] as const;

export const Dashboard = () => {
  type ChangeType = 'pairwise' | 'metadata' | 'pairwise+metadata' | 'none';

  const [formData, setFormData] =
    useState<Partial<ClientWithoutSensitiveData> | null>(defaultFormData);
  const [pairWiseData, setPairWiseData] = useState<Partial<ValidatePlanSchema>>(
    {
      apiKeyId: '',
      apiKeyValue: '',
    }
  );
  const [errorUi, setErrorUi] = useState<ClientErrors | null>(null);
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const { showNotification } = useNotification();
  const openConfirm = () => setIsConfirmOpen(true);
  const closeConfirm = () => setIsConfirmOpen(false);
  const [contentDialog, setContentDialog] = useState<string>('');
  const { isModalOpen, openModal, closeModal } = useModalManager();
  const enablingPairWiseDialogContent =
    'By enabling PairWise One Identity, it will use PDV to obtain the subject identifier.';
  const disablingPairWiseDialogContent =
    'Disabling PairWise pseudonymous will stop sharing communication from PDV Building Block and OI.';
  const metadataDialogContent =
    'Updating SPID Level or SAML Attributes or SPID Minors fields may require re-sharing them with the SPID and CIE authorities (AgID and IPZS).';
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
      setFormData({
        ...clientDataWithoutSensitiveData(fetchedClientData),
        // set default value for samlBinding if not present in the fetched data
        samlBinding: fetchedClientData.samlBinding ?? SamlBinding.HTTP_POST,
      });

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
        showNotification('PairWise key validated successfully', 'success');
      } else {
        setErrorUi(null);
        showNotification('PairWise key validation failed', 'error');
      }
    }
    if (validateError) {
      console.error('Error validatig api key:', validateError);
      setErrorUi(validateError as unknown as ValidateError);
      showNotification('PairWise error validating api key', 'error');
    }
    if (planListError) {
      setErrorUi(planListError as unknown as PlanErrors);
      showNotification('PairWise error retrieving plan list', 'error');
    }
  }, [
    validateError,
    isValidated,
    planListError,
    validationIsValid,
    showNotification,
  ]);

  useEffect(() => {
    if (updateError) {
      console.error('Error updating client:', updateError);
      setErrorUi(updateError as unknown as ClientErrors);
      showNotification('Error updating client', 'error');
    }
    if (isUpdated) {
      setErrorUi(null);
      const message = isCreating
        ? 'Client created successfully'
        : 'Client updated successfully';
      showNotification(message, 'success');

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
    showNotification,
  ]);

  const isFormValid = () => {
    // TODO: if clientSchema inside api.ts is adjusted to reflect the actual optional and required fields, we can use:
    // const isFormValid = () => clientSchema.safeParse(formData).success;
    return (
      !!formData?.clientName &&
      !!formData?.redirectUris?.length &&
      !!formData?.defaultAcrValues?.length &&
      !!formData?.samlRequestedAttributes?.length &&
      !!formData?.samlBinding
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

  const getChangeType = (pairwise: boolean, metadata: boolean): ChangeType => {
    if (pairwise && metadata) return 'pairwise+metadata';
    if (pairwise) return 'pairwise';
    if (metadata) return 'metadata';
    return 'none';
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
      !isEqual(
        formData?.defaultAcrValues,
        fetchedClientData?.defaultAcrValues
      ) ||
      !isEqual(formData?.spidMinors, fetchedClientData?.spidMinors) ||
      !isEqual(formData?.minAge, fetchedClientData?.minAge) ||
      !isEqual(formData?.maxAge, fetchedClientData?.maxAge) ||
      !isEqual(formData?.ageParentAuth, fetchedClientData?.ageParentAuth);

    const pairWiseText =
      isPairwiseChanged &&
      (formData?.pairwise
        ? enablingPairWiseDialogContent
        : disablingPairWiseDialogContent);

    const changeType = getChangeType(isPairwiseChanged, isMetadataChanged);

    switch (changeType) {
      case 'pairwise+metadata':
        return `${metadataDialogContent}\n\n${pairWiseText} ${confirmText}`;
      case 'pairwise':
        return `${pairWiseText} ${confirmText}`;
      case 'metadata':
        return `${metadataDialogContent} ${confirmText}`;
      case 'none':
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

  const checkEnableSpidMinors = (): boolean =>
    !!formData?.spidMinors && !formData?.minAge;

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

          <FormControl
            fullWidth
            margin="normal"
            required
            error={!!(errorUi as ClientErrors)?.samlBinding?._errors}
          >
            <InputLabel id="saml-binding-label">Saml Binding</InputLabel>
            <Select
              labelId="saml-binding-label"
              id="saml-binding-select"
              value={formData?.samlBinding || SamlBinding.HTTP_POST}
              label="Saml Binding"
              onChange={(e) =>
                setFormData((prev) => ({
                  ...prev,
                  samlBinding: e.target.value as SamlBinding,
                }))
              }
            >
              <MenuItem value={SamlBinding.HTTP_POST}>
                {SamlBinding.HTTP_POST}
              </MenuItem>
              <MenuItem value={SamlBinding.HTTP_REDIRECT}>
                {SamlBinding.HTTP_REDIRECT}
              </MenuItem>
            </Select>
            <FormHelperText>
              {(errorUi as ClientErrors)?.samlBinding?._errors}
            </FormHelperText>
            <FormHelperText>
              Method used to send the SAML authentication request to Identity
              Providers.
            </FormHelperText>
          </FormControl>

          <ToggleSection
            name="requiredSameIdp"
            label="Required Same IDP"
            checked={formData?.requiredSameIdp || false}
            onChange={handleChange('requiredSameIdp')}
            tooltipText={
              <TooltipContentWithLink
                text="Same IDP is a function that will return a custom request indicating whether the user has logged in using the same IDP as the previous time."
                infoUrl="https://pagopa.atlassian.net/wiki/spaces/OI/pages/1560477700/RFC+OI-004+Check+last+used+IDP+-+OTP"
              />
            }
          />

          <ToggleSection
            name="pairWise"
            label="Pairwise Enabled"
            checked={formData?.pairwise || false}
            onChange={handleChange('pairwise')}
            withDivider
            tooltipText={
              <TooltipContentWithLink
                text="Pairwise pseudonymous is an OIDC object used to replace the subject identifier. One Identity will leverage on PDV Building block to calculate or obtain it."
                infoUrl="https://pagopa.atlassian.net/wiki/spaces/OI/pages/2101936152/OI+-+Integrazione+PDV"
              />
            }
          />
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
                      required
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
                    required
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
            </Box>
          )}

          <ToggleSection
            name="spidMinors"
            label="SPID Minors"
            checked={formData?.spidMinors || false}
            onChange={(e) => {
              const checked = e.target.checked;
              setFormData((prev) => ({
                ...prev,
                spidMinors: checked,
                ...(!checked && {
                  minAge: undefined,
                  maxAge: undefined,
                  ageParentAuth: undefined,
                }),
              }));
              if (!checked) {
                // reset spid minors field errors
                setErrorUi((prev) => {
                  if (!prev) return prev;
                  const {
                    minAge: _minAge,
                    maxAge: _maxAge,
                    ageParentAuth: _ageParentAuth,
                    ...rest
                  } = prev;
                  return rest as ClientErrors;
                });
              }
            }}
            withDivider
            tooltipText={
              <TooltipContentWithLink
                text="SPID Minors allows the service to authenticate minor users (under 18). When enabled, you can define an allowed age range for minor authentication."
                infoUrl="https://pagopa.atlassian.net/wiki/spaces/OI/pages/2965078170/RFC+OI-016+-+Supporto+SPID+per+minori"
              />
            }
          />
          {formData?.spidMinors && (
            <Box
              sx={{
                display: 'flex',
                flexDirection: 'row',
                gap: 2,
                mt: 2,
                width: '100%',
              }}
            >
              <TextField
                label="Min Age"
                fullWidth
                required
                type="number"
                inputProps={{ min: 5, max: 17 }}
                value={formData?.minAge ?? ''}
                onChange={(e) => {
                  const val = e.target.value;
                  setFormData((prev) => ({
                    ...prev,
                    minAge: val === '' ? undefined : parseInt(val, 10),
                  }));
                }}
                error={!!(errorUi as ClientErrors)?.minAge?._errors}
                helperText={(errorUi as ClientErrors)?.minAge?._errors}
              />
              <TextField
                label="Max Age"
                fullWidth
                type="number"
                inputProps={{ min: 5, max: 999 }}
                value={formData?.maxAge ?? ''}
                onChange={(e) => {
                  const val = e.target.value;
                  setFormData((prev) => ({
                    ...prev,
                    maxAge: val === '' ? undefined : parseInt(val, 10),
                  }));
                }}
                error={!!(errorUi as ClientErrors)?.maxAge?._errors}
                helperText={(errorUi as ClientErrors)?.maxAge?._errors}
              />
              <TextField
                label="Age Parent Auth"
                fullWidth
                type="number"
                inputProps={{ min: 6, max: 17 }}
                value={formData?.ageParentAuth ?? ''}
                onChange={(e) => {
                  const val = e.target.value;
                  setFormData((prev) => ({
                    ...prev,
                    ageParentAuth: val === '' ? undefined : parseInt(val, 10),
                  }));
                }}
                error={!!(errorUi as ClientErrors)?.ageParentAuth?._errors}
                helperText={(errorUi as ClientErrors)?.ageParentAuth?._errors}
              />
            </Box>
          )}

          <ToggleSection
            name="eidasIndex"
            label="eIDAS support"
            checked={!!formData?.eidasIndex}
            onChange={(e) => {
              const checked = e.target.checked;
              setFormData((prev) => ({
                ...prev,
                eidasIndex: checked
                  ? (prev?.eidasIndex ?? EidasAttributeSet.MINIMUM)
                  : undefined,
              }));
              if (!checked) {
                setErrorUi((prev) => {
                  if (!prev) return prev;
                  const { eidasIndex: _eidasIndex, ...rest } = prev;
                  return rest as ClientErrors;
                });
              }
            }}
            withDivider
            tooltipText={
              <TooltipContentWithLink
                text="Enable access to the international services for citizens of EU member states."
                infoUrl="https://pagopa.atlassian.net/wiki/spaces/OI/pages/3013574677/RFC+OI-018+-+Supporto+nodo+europeo+eIDAS"
              />
            }
          />
          {!!formData?.eidasIndex && (
            <FormControl
              fullWidth
              margin="normal"
              required
              error={!!(errorUi as ClientErrors)?.eidasIndex?._errors}
            >
              <InputLabel id="eidas-index-label">
                eIDAS attribute set
              </InputLabel>
              <Select
                labelId="eidas-index-label"
                id="eidas-index-select"
                value={formData?.eidasIndex ?? EidasAttributeSet.MINIMUM}
                label="eIDAS attribute set"
                onChange={(e) =>
                  setFormData((prev) => ({
                    ...prev,
                    eidasIndex: Number(e.target.value) as EidasAttributeSet,
                  }))
                }
              >
                {EIDAS_ATTRIBUTE_SET_OPTIONS.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                      <Typography variant="body1">{option.label}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {option.description}
                      </Typography>
                    </Box>
                  </MenuItem>
                ))}
              </Select>
              <FormHelperText>
                {(errorUi as ClientErrors)?.eidasIndex?._errors}
              </FormHelperText>
            </FormControl>
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
              checkEnableSpidMinors() ||
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
    </PageContainer>
  );
};
