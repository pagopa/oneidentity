import {
  Box,
  FormControlLabel,
  Grid,
  Switch,
  TextField,
  Typography,
} from '@mui/material';
import { Client, ClientErrors } from '../../../types/api';

type ClientSettingsProps = {
  clientData: Client | undefined | null;
  clientID?: string;
  onClientDataChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  errorUi?: ClientErrors | null;
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
          error={!!(errorUi as ClientErrors)?.a11yUri?._errors}
          helperText={(errorUi as ClientErrors)?.a11yUri?._errors}
        />
      </Grid>
      <Grid item xs={12}>
        <FormControlLabel
          control={
            <Switch
              sx={{ mr: 2, ml: 1 }}
              name="backButtonEnabled"
              checked={clientData?.backButtonEnabled || false}
              onChange={onClientDataChange}
            />
          }
          label="Back button enabled"
        />
      </Grid>
    </Grid>
  </Box>
);
