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
} from '@mui/material';
import { useLocation, useParams, useNavigate } from 'react-router-dom';
import {
  SamlAttribute,
  IdpUser,
  UserErrors,
  idpUserSchema,
} from '../../../types/api';
import { useAuth } from 'react-oidc-context';
import { Notify } from '../../../components/Notify';
import { useClient } from '../../../hooks/useClient';
import { fromPairs } from 'lodash';
import { ROUTE_PATH } from '../../../utils/constants';
import SamlAttributesSelectInput from '../../../components/SamlAttributesSelectInput';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import AddIcon from '@mui/icons-material/Add';
import SaveIcon from '@mui/icons-material/Save';
import { PageContainer } from '../../../components/PageContainer';
import { ContentBox } from '../../../components/ContentBox';

const SamlAttributeValueFields = ({
  attributes,
  onChange,
}: {
  attributes: Array<[string, string]>;
  onChange: (attribute: string, value: string) => void;
}) => {
  return (
    <Box sx={{ mt: '8px' }}>
      {attributes.map(([attribute, value]) => (
        <TextField
          sx={{ mt: '16px', mb: '8px' }}
          key={attribute}
          label={`Value for ${attribute}`}
          value={value}
          onChange={(e) => onChange(attribute, e.target.value)}
          margin="dense"
          fullWidth
        />
      ))}
    </Box>
  );
};

export const AddOrUpdateUser = () => {
  const { user } = useAuth();
  const userId = user?.profile.sub;
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
        message:
          addClientUsersError.message === 'User already exists'
            ? 'User already exists'
            : 'Error creating user',
        severity: 'error',
      });
    }
    if (updateClientUsersError) {
      console.error('Error update user:', updateClientUsersError);
      setErrorUi(updateClientUsersError as unknown as UserErrors);
      setNotify({
        open: true,
        message: 'Error updating user',
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
  }, [isUserCreated, isUserUpdated, navigate]);

  const isFormValid = () => idpUserSchema.safeParse(formData).success;

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
      if (!userId) {
        console.error('Missing user_id');
        return;
      }
      createClientUsersMutation({
        data: { ...(formData as IdpUser), user_id: userId },
      });
    }
  };

  const handleChange =
    (field: keyof IdpUser) => (e: React.ChangeEvent<HTMLInputElement>) => {
      setFormData((prev) => ({ ...prev, [field]: e.target.value }));
    };

  return (
    <PageContainer>
      <Box component="form" onSubmit={handleSubmit}>
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

          <SamlAttributesSelectInput
            attributeSelectValues={
              Object.keys(
                formData?.samlAttributes || {}
              ) as Array<SamlAttribute>
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
            <SamlAttributeValueFields
              attributes={Object.entries(formData?.samlAttributes || {})}
              onChange={(attribute, value) => {
                setFormData((prev) => ({
                  ...prev,
                  samlAttributes: {
                    ...(prev?.samlAttributes || {}),
                    [attribute]: value,
                  },
                }));
              }}
            />
          </SamlAttributesSelectInput>
        </ContentBox>

        <Button
          type="submit"
          variant="contained"
          sx={{ mt: 3 }}
          data-testid="submit-button"
          startIcon={isEditMode ? <SaveIcon /> : <AddIcon />}
          disabled={!isFormValid() || isCreatingUser || isUpdatingUser}
        >
          {isEditMode ? 'Update User' : 'Add User'}
        </Button>
      </Box>

      <Backdrop open={isCreatingUser || isUpdatingUser}>
        <CircularProgress color="secondary" />
      </Backdrop>

      <Notify
        open={notify.open}
        message={notify.message}
        severity={notify.severity}
        handleOpen={(open) => setNotify({ ...notify, open })}
      />
    </PageContainer>
  );
};
