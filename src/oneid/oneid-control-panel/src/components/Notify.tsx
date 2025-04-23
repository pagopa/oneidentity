import { Close } from '@mui/icons-material';
import { Alert, Box, IconButton, Snackbar } from '@mui/material';

export type Notify = {
  open: boolean;
  message?: string;
  severity?: 'success' | 'error';
};

type NotifyProps = Notify & {
  handleOpen: (open: boolean) => void;
};

export const Notify = ({
  open,
  message,
  severity,
  handleOpen,
}: NotifyProps) => {
  return (
    <Snackbar
      open={open}
      anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
    >
      <Alert
        action={
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <IconButton
              aria-label="close"
              color="inherit"
              onClick={() => handleOpen(false)}
              size="small"
            >
              <Close fontSize="small" />
            </IconButton>
          </Box>
        }
        severity={severity}
        sx={{
          width: '100%',
        }}
        variant="outlined"
      >
        {message}
      </Alert>
    </Snackbar>
  );
};
