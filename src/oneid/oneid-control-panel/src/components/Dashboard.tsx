import { useState, useEffect } from 'react';
import {
  Box,
  TextField,
  Button,
  Typography,
  AppBar,
  Toolbar,
  Chip,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  OutlinedInput,
  Alert,
  CircularProgress,
} from '@mui/material';
// import { useClient, useUpdateClient } from '../hooks/useClient';
import { useNavigate } from 'react-router-dom';
import { ClientData, SpidLevel, SamlAttribute } from '../types/api';
import { useAuth } from 'react-oidc-context';
import { useAuthSignoutRedirect } from '../services/api';
import { ENV } from '../utils/env';
import { red } from '@mui/material/colors';
import { r } from 'react-router/dist/development/fog-of-war-oa9CGk10';

export const Dashboard = () => {
  const { user, isAuthenticated, removeUser, signoutRedirect } = useAuth();
  const [formData, setFormData] = useState<Partial<ClientData> | null>(
    user?.profile
  );
  const navigate = useNavigate();

  // const {
  //   clientData: fetchedClientData,
  //   isLoading: isLoadingClient,
  //   error: fetchError,
  // } = useClient('asd'); // Replace 'asd' with the actual client ID

  // useEffect(() => {
  //   if (fetchedClientData) {
  //     setFormData(fetchedClientData);
  //   }
  // }, [fetchedClientData]);

  // const {
  //   mutate: updateClient,
  //   isPending: isUpdating,
  //   error: updateError,
  // } = useUpdateClient();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData) return;

    const {
      client_id,
      client_secret,
      client_id_issued_at,
      client_secret_expires_at,
      ...submitData
    } = formData;

    // updateClient({
    //   data: submitData,
    //   clientId: client_id,
    // });
  };

  function signout() {
    return signoutRedirect({
      // aws cognito extras
      extraQueryParams: {
        client_id: ENV.OIDC.CLIENT_ID,
        logout_uri: ENV.OIDC.REDIRECT_URI,
        redirect_uri: ENV.OIDC.REDIRECT_URI,
        response_type: ENV.OIDC.RESPONSE_TYPE,
        scope: ENV.OIDC.SCOPE,
      },
    });
  }
  const handleChange =
    (field: keyof ClientData) => (e: React.ChangeEvent<HTMLInputElement>) => {
      setFormData((prev) => ({ ...prev, [field]: e.target.value }));
    };

  const handleLogout = () => {
    removeUser();
    signout();
  };

  const isFormValid = () => {
    return !!formData?.client_name;
  };

  // if (isLoadingClient) {
  //   return (
  //     <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
  //       <CircularProgress />
  //     </Box>
  //   );
  // }

  // if (fetchError) {
  //   return (
  //     <Box sx={{ mt: 4 }}>
  //       <Alert severity="error">
  //         {fetchError instanceof Error
  //           ? fetchError.message
  //           : 'An error occurred'}
  //       </Alert>
  //     </Box>
  //   );
  // }

  // if (!formData) return null;

  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <Typography
            variant="h6"
            component="div"
            color={'white'}
            sx={{ flexGrow: 1 }}
          >
            OneIdentity Client Management
          </Typography>
          <Button color="inherit" onClick={handleLogout}>
            Logout
          </Button>
        </Toolbar>
      </AppBar>
      <Typography variant="h6" sx={{ mt: 2 }}>
        User: {user?.profile?.email}
      </Typography>
      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{ p: 3, maxWidth: 800, mx: 'auto' }}
      >
        {/* {updateError && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {updateError instanceof Error
              ? updateError.message
              : 'An error occurred'}
          </Alert>
        )} */}

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
        />

        <TextField
          fullWidth
          label="Logo URI"
          value={formData?.logo_uri || ''}
          onChange={handleChange('logo_uri')}
          margin="normal"
        />

        <TextField
          fullWidth
          label="Policy URI"
          value={formData?.policy_uri || ''}
          onChange={handleChange('policy_uri')}
          margin="normal"
        />

        <TextField
          fullWidth
          label="Terms of Service URI"
          value={formData?.tos_uri || ''}
          onChange={handleChange('tos_uri')}
          margin="normal"
        />

        <FormControl fullWidth margin="normal">
          <InputLabel>Redirect URIs</InputLabel>
          <Select
            multiple
            value={formData?.redirect_uris || []}
            onChange={(e) =>
              setFormData((prev) => ({
                ...prev,
                redirect_uris: e.target.value as Array<string>,
              }))
            }
            input={<OutlinedInput label="Redirect URIs" />}
            renderValue={(selected) => (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {selected.map((value) => (
                  <Chip key={value} label={value} />
                ))}
              </Box>
            )}
          >
            {formData?.redirect_uris?.map((uri) => (
              <MenuItem key={uri} value={uri}>
                {uri}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>SPID Level</InputLabel>
          <Select
            multiple
            value={formData?.default_acr_values || []}
            onChange={(e) =>
              setFormData((prev) => ({
                ...prev,
                default_acr_values: e.target.value as Array<SpidLevel>,
              }))
            }
            input={<OutlinedInput label="SPID Level" />}
            data-testid="spid-level-select"
          >
            {Object.values(SpidLevel).map((level) => (
              <MenuItem key={level} value={level}>
                {level.replace('https://www.spid.gov.it/Spid', 'Level ')}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>SAML Attributes</InputLabel>
          <Select
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
        </FormControl>

        <Button
          type="submit"
          variant="contained"
          sx={{ mt: 2 }}
          // disabled={isUpdating || !isFormValid()}
        >
          as
          {/* {isUpdating ? 'Saving...' : 'Save Changes'} */}
        </Button>
        {isAuthenticated && (
          <Button
            variant="contained"
            color="primary"
            fullWidth
            sx={{ mt: 2 }}
            onClick={handleLogout}
          >
            Logout
          </Button>
        )}
      </Box>
    </>
  );
};
