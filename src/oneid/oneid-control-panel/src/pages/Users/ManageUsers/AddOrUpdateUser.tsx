import { useEffect, useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  InputAdornment,
  IconButton,
  Backdrop,
  CircularProgress,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormHelperText,
} from '@mui/material';
import { useLocation, useParams, useNavigate } from 'react-router-dom';
import { SamlAttribute, IdpUser, UserErrors } from '../../../types/api';
import { useClient } from '../../../hooks/useClient';
import { useRegister } from '../../../hooks/useRegister';
import { useNotification } from '../../../context/NotificationContext';
import { omit } from 'lodash';
import { ROUTE_PATH } from '../../../utils/constants';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import AddIcon from '@mui/icons-material/Add';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import SaveIcon from '@mui/icons-material/Save';
import { PageContainer } from '../../../components/PageContainer';
import { ContentBox } from '../../../components/ContentBox';
import {
  calculateAgeFromDateOfBirth,
  userFormSchema,
} from '../../../utils/user';

const mapUserValidationError = (message: string): UserErrors | null => {
  const normalizedMessage = message.toLowerCase();

  if (
    normalizedMessage === 'username, password and saml_attributes are required'
  ) {
    return {
      _errors: [],
      username: { _errors: [message] },
      password: { _errors: [message] },
      samlAttributes: { _errors: [message] },
    } as unknown as UserErrors;
  }

  if (normalizedMessage.includes('username format is invalid')) {
    return {
      _errors: [],
      username: { _errors: [message] },
    } as unknown as UserErrors;
  }

  if (normalizedMessage.includes('password format is invalid')) {
    return {
      _errors: [],
      password: { _errors: [message] },
    } as unknown as UserErrors;
  }

  if (
    normalizedMessage.includes('samlattributes') ||
    normalizedMessage.includes('saml_attributes')
  ) {
    return {
      _errors: [],
      samlAttributes: { _errors: [message] },
    } as unknown as UserErrors;
  }

  return null;
};

const SamlAttributeValueFields = ({
  attributes,
  onChange,
  errors,
  required = false,
  onRemove,
}: {
  attributes: Array<[SamlAttribute, string]>;
  onChange: (attribute: SamlAttribute, value: string) => void;
  errors?: UserErrors['samlAttributes'];
  required?: boolean;
  onRemove?: (attribute: SamlAttribute) => void;
}) => {
  return (
    <Box sx={{ mt: '8px' }}>
      {attributes.map(([attribute, value]) => (
        <TextField
          sx={{ mt: '16px', mb: '8px' }}
          key={attribute}
          label={attribute}
          value={value}
          onChange={(e) => onChange(attribute, e.target.value)}
          type={attribute === SamlAttribute.DATE_OF_BIRTH ? 'date' : 'text'}
          InputLabelProps={
            attribute === SamlAttribute.DATE_OF_BIRTH
              ? { shrink: true }
              : undefined
          }
          required={required}
          error={!!errors?.[attribute]?._errors.length}
          helperText={errors?.[attribute]?._errors}
          InputProps={
            onRemove
              ? {
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        aria-label={`Remove ${attribute}`}
                        onClick={() => onRemove(attribute)}
                        edge="end"
                      >
                        <DeleteOutlineIcon />
                      </IconButton>
                    </InputAdornment>
                  ),
                }
              : undefined
          }
          margin="dense"
          fullWidth
        />
      ))}
    </Box>
  );
};

export const AddOrUpdateUser = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const userToEdit = location.state?.userToEdit as IdpUser | undefined;
  const { id: usernameQueryParam } = useParams();
  const isEditMode = !!usernameQueryParam;
  const [formData, setFormData] = useState<Partial<IdpUser>>({});
  const [errorUi, setErrorUi] = useState<UserErrors | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const [isAdditionalDialogOpen, setIsAdditionalDialogOpen] = useState(false);
  const [hasConfirmedAdditional, setHasConfirmedAdditional] = useState(false);
  const [selectedAttribute, setSelectedAttribute] = useState<
    SamlAttribute | ''
  >('');
  const { showNotification } = useNotification();
  const {
    clientQuery: {
      data: clientData,
      isSuccess: isClientLoaded,
      isFetching: isFetchingClient,
      error: clientError,
      refetch: refetchClient,
    },
  } = useRegister();
  const requiredAttributes = [
    ...new Set(clientData?.samlRequestedAttributes ?? []),
  ];
  const isClientReady =
    isClientLoaded &&
    !isFetchingClient &&
    Array.isArray(clientData?.samlRequestedAttributes);
  const additionalAttributes = Object.values(SamlAttribute).filter(
    (attribute) =>
      !requiredAttributes.includes(attribute) &&
      formData.samlAttributes?.[attribute] !== undefined
  );
  const availableAttributes = Object.values(SamlAttribute).filter(
    (attribute) =>
      !requiredAttributes.includes(attribute) &&
      !additionalAttributes.includes(attribute)
  );
  const hasDateOfBirth =
    requiredAttributes.includes(SamlAttribute.DATE_OF_BIRTH) ||
    formData.samlAttributes?.[SamlAttribute.DATE_OF_BIRTH] !== undefined;
  const dateOfBirthAge = hasDateOfBirth
    ? calculateAgeFromDateOfBirth(
        formData.samlAttributes?.[SamlAttribute.DATE_OF_BIRTH] ?? ''
      )
    : undefined;
  const age = hasDateOfBirth
    ? dateOfBirthAge?.success
      ? dateOfBirthAge.age
      : undefined
    : formData.age;
  const ageExplanation =
    'Test parameter used by the internal IdP to simulate the birth-date data stored by real IdPs. Calculated automatically from dateOfBirth when present; otherwise, enter it manually.';
  const additionalWarning =
    'These attributes will only be added to this test user. OneIdentity will not propagate them to the client unless they are also added to the requested SAML attributes in the Register tab. This action does not change the client configuration.';

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
      setErrorUi(
        mapUserValidationError(addClientUsersError.message) ??
          (addClientUsersError as unknown as UserErrors)
      );
      showNotification(
        addClientUsersError.message === 'User already exists'
          ? 'User already exists'
          : 'Error creating user',
        'error'
      );
    }
    if (updateClientUsersError) {
      console.error('Error update user:', updateClientUsersError);
      setErrorUi(
        mapUserValidationError(updateClientUsersError.message) ??
          (updateClientUsersError as unknown as UserErrors)
      );
      showNotification('Error updating user', 'error');
    }
  }, [addClientUsersError, updateClientUsersError, showNotification]);

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
  }, [isUserCreated, isUserUpdated, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isClientReady) {
      showNotification(
        'Wait for the client configuration to load before saving the user.',
        'error'
      );
      return;
    }

    const validationResult = userFormSchema.safeParse({
      ...formData,
      age,
      samlAttributes: {
        ...Object.fromEntries(
          requiredAttributes.map((attribute) => [attribute, ''])
        ),
        ...formData.samlAttributes,
      },
    });

    if (!validationResult.success) {
      setErrorUi(validationResult.error.format());
      console.error('Form is not valid');
      return;
    }

    setErrorUi(null);

    if (isEditMode) {
      updateClientUsersMutation({
        data: validationResult.data,
        username: validationResult.data.username,
      });
    } else {
      createClientUsersMutation({
        data: validationResult.data,
      });
    }
  };

  const handleChange =
    (field: keyof IdpUser) => (e: React.ChangeEvent<HTMLInputElement>) => {
      setFormData((prev) => ({ ...prev, [field]: e.target.value }));
    };

  const handleAttributeChange = (attribute: SamlAttribute, value: string) => {
    setFormData((prev) => ({
      ...prev,
      samlAttributes: {
        ...prev.samlAttributes,
        [attribute]: value,
      },
    }));
    setErrorUi((prev) =>
      prev
        ? {
            ...prev,
            samlAttributes:
              prev.samlAttributes && omit(prev.samlAttributes, attribute),
            ...(attribute === SamlAttribute.DATE_OF_BIRTH
              ? { age: undefined }
              : {}),
          }
        : null
    );
  };

  const closeAdditionalDialog = () => {
    setIsAdditionalDialogOpen(false);
    setHasConfirmedAdditional(false);
    setSelectedAttribute('');
  };

  return (
    <PageContainer>
      <Box component="form" onSubmit={handleSubmit} noValidate>
        <ContentBox>
          <Typography variant="h5" gutterBottom>
            User Data
          </Typography>

          <TextField
            required
            fullWidth
            label="Username"
            value={formData?.username || ''}
            margin="normal"
            onChange={handleChange('username')}
            error={!!(errorUi as UserErrors)?.username?._errors}
            helperText={(errorUi as UserErrors)?.username?._errors}
            disabled={isEditMode}
            onKeyDown={(e) => {
              if (e.key === ' ') {
                e.preventDefault();
              }
            }}
          />

          <TextField
            fullWidth
            required
            type={'text'}
            label="Password"
            value={formData?.password || ''}
            onChange={handleChange('password')}
            margin="normal"
            error={!!(errorUi as UserErrors)?.password?._errors}
            helperText={(errorUi as UserErrors)?.password?._errors}
            disabled={isEditMode}
            InputProps={{
              sx: !showPassword
                ? {
                    WebkitTextSecurity: 'disc',
                    MozTextSecurity: 'disc',
                  }
                : {},
              ...(formData.password
                ? {
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          aria-label="toggle password visibility"
                          onClick={() => setShowPassword((prev) => !prev)}
                          edge="end"
                          size="small"
                        >
                          {showPassword ? (
                            <VisibilityOff
                              data-testid="VisibilityOffIcon"
                              fontSize="small"
                              sx={{ color: 'grey' }}
                            />
                          ) : (
                            <Visibility
                              data-testid="VisibilityOnIcon"
                              fontSize="small"
                              sx={{ color: 'grey' }}
                            />
                          )}
                        </IconButton>
                      </InputAdornment>
                    ),
                  }
                : {}),
            }}
          />

          <TextField
            required
            fullWidth
            label="Age"
            type="number"
            inputProps={{ min: 0, max: 999 }}
            InputProps={{ readOnly: hasDateOfBirth }}
            value={age ?? ''}
            onChange={(e) => {
              const val = e.target.value;
              setFormData((prev) => ({
                ...prev,
                age: val === '' ? undefined : Number(val),
              }));
            }}
            margin="normal"
            error={!!errorUi?.age?._errors.length}
            helperText={[ageExplanation, ...(errorUi?.age?._errors ?? [])].join(
              ' '
            )}
          />

          {isFetchingClient && (
            <Alert severity="info" sx={{ mt: 2 }}>
              Loading client SAML attributes...
            </Alert>
          )}
          {!isFetchingClient && !isClientReady && (
            <Alert
              severity="error"
              sx={{ mt: 2 }}
              action={
                <Button color="inherit" onClick={() => refetchClient()}>
                  Retry
                </Button>
              }
            >
              Unable to load the client SAML attributes.
              {clientError && ` ${clientError.message}`}
            </Alert>
          )}
          {isClientReady && (
            <>
              <Typography variant="h6" sx={{ mt: 3 }}>
                Required SAML attributes
              </Typography>
              <Typography variant="body2" color="text.secondary">
                These attributes are requested by the client. All values are
                required.
              </Typography>
              <SamlAttributeValueFields
                attributes={requiredAttributes.map((attribute) => [
                  attribute,
                  formData.samlAttributes?.[attribute] ?? '',
                ])}
                onChange={handleAttributeChange}
                errors={errorUi?.samlAttributes}
                required
              />
            </>
          )}
          {!!errorUi?.samlAttributes?._errors.length && (
            <FormHelperText error>
              {errorUi.samlAttributes._errors}
            </FormHelperText>
          )}
        </ContentBox>

        {isClientReady && additionalAttributes.length > 0 && (
          <Box sx={{ mt: 3 }}>
            <ContentBox>
              <Typography variant="h6" gutterBottom>
                Additional SAML attributes
              </Typography>
              <Alert severity="warning">{additionalWarning}</Alert>
              <SamlAttributeValueFields
                attributes={additionalAttributes.map((attribute) => [
                  attribute,
                  formData.samlAttributes?.[attribute] ?? '',
                ])}
                onChange={handleAttributeChange}
                errors={errorUi?.samlAttributes}
                onRemove={(attribute) => {
                  setFormData((prev) => ({
                    ...prev,
                    samlAttributes: omit(prev.samlAttributes, attribute),
                    age:
                      attribute === SamlAttribute.DATE_OF_BIRTH &&
                      dateOfBirthAge?.success
                        ? dateOfBirthAge.age
                        : prev.age,
                  }));
                }}
              />
            </ContentBox>
          </Box>
        )}

        <Box
          sx={{
            mt: 3,
            display: 'flex',
            justifyContent: 'space-between',
            gap: 2,
            flexWrap: 'wrap',
          }}
        >
          <Button
            type="submit"
            variant="contained"
            data-testid="submit-button"
            startIcon={isEditMode ? <SaveIcon /> : <AddIcon />}
            disabled={isCreatingUser || isUpdatingUser || !isClientReady}
          >
            {isEditMode ? 'Update User' : 'Add User'}
          </Button>
          <Button
            type="button"
            variant="outlined"
            startIcon={<AddIcon />}
            disabled={
              !isClientReady ||
              isCreatingUser ||
              isUpdatingUser ||
              availableAttributes.length === 0
            }
            onClick={() => setIsAdditionalDialogOpen(true)}
          >
            Add additional SAML attributes
          </Button>
        </Box>
      </Box>

      <Dialog
        open={isAdditionalDialogOpen}
        onClose={closeAdditionalDialog}
        aria-labelledby="additional-attributes-title"
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle id="additional-attributes-title">
          Add additional SAML attributes
        </DialogTitle>
        <DialogContent>
          <Alert severity="warning">{additionalWarning}</Alert>
          {hasConfirmedAdditional && (
            <FormControl fullWidth margin="normal">
              <InputLabel id="additional-attribute-label">
                Additional SAML attribute
              </InputLabel>
              <Select
                labelId="additional-attribute-label"
                label="Additional SAML attribute"
                value={selectedAttribute}
                onChange={(event) => {
                  const attribute = availableAttributes.find(
                    (value) => value === event.target.value
                  );
                  setSelectedAttribute(attribute ?? '');
                }}
              >
                {availableAttributes.map((attribute) => (
                  <MenuItem key={attribute} value={attribute}>
                    {attribute}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={closeAdditionalDialog}>Cancel</Button>
          {hasConfirmedAdditional ? (
            <Button
              variant="contained"
              disabled={
                !isClientReady ||
                !selectedAttribute ||
                !availableAttributes.includes(selectedAttribute)
              }
              onClick={() => {
                if (
                  selectedAttribute &&
                  availableAttributes.includes(selectedAttribute)
                ) {
                  handleAttributeChange(selectedAttribute, '');
                  closeAdditionalDialog();
                }
              }}
            >
              Add attribute
            </Button>
          ) : (
            <Button
              variant="contained"
              onClick={() => setHasConfirmedAdditional(true)}
            >
              Continue
            </Button>
          )}
        </DialogActions>
      </Dialog>

      <Backdrop open={isCreatingUser || isUpdatingUser}>
        <CircularProgress color="secondary" />
      </Backdrop>
    </PageContainer>
  );
};
