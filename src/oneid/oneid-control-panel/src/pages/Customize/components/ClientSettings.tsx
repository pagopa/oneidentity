import {
  Box,
  FormControlLabel,
  Grid,
  Switch,
  TextField,
  Typography,
} from '@mui/material';
import { ClientFE, ClientFEErrors } from '../../../types/api';

type ClientSettingsProps = {
  clientData: ClientFE | undefined | null;
  clientID?: string;
  onClientDataChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  errorUi?: ClientFEErrors | null;
};

export const ClientSettings = ({
  clientData,
  clientID,
  onClientDataChange,
  errorUi,
}: ClientSettingsProps) => (
  <Box mb={4}>
    <Typography variant="h6" component="h2" gutterBottom>
      General Settings
    </Typography>
    <Grid container spacing={3} mb={3}>
      <Grid item xs={12} sm={6}>
        <TextField
          label="Client ID"
          name="clientID"
          value={clientID}
          fullWidth
          disabled
          variant="filled"
        />
      </Grid>
      <Grid item xs={12} sm={6}>
        <TextField
          label="Accessibility URI"
          name="a11yUri"
          value={clientData?.a11yUri || ''}
          onChange={onClientDataChange}
          fullWidth
          error={!!(errorUi as ClientFEErrors)?.a11yUri?._errors}
          helperText={(errorUi as ClientFEErrors)?.a11yUri?._errors}
        />
      </Grid>
      <Grid item xs={12}>
        <FormControlLabel
          control={
            <Switch
              name="backButtonEnabled"
              checked={clientData?.backButtonEnabled || false}
              onChange={onClientDataChange}
            />
          }
          label="Back Button Enabled"
        />
      </Grid>
    </Grid>
  </Box>
);
