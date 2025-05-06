import {
  Button,
  Modal,
  Box,
  Typography,
  TextField,
  InputAdornment,
  IconButton,
} from '@mui/material';
import { ClientRegisteredData } from '../types/api';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import React from 'react';

const style = {
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  width: 400,
  bgcolor: 'background.paper',
  p: 4,
};

type SecretModalProps = {
  title: string;
  onClose: () => void;
  open: boolean;
  data: ClientRegisteredData;
};

export const SecretModal = ({
  title,
  onClose,
  open,
  data,
}: SecretModalProps) => {
  const [showPassword, setShowPassword] = React.useState(false);

  const handleClickShowPassword = () => setShowPassword((show) => !show);

  const handleMouseDownPassword = (
    event: React.MouseEvent<HTMLButtonElement>
  ) => {
    event.preventDefault();
  };
  return (
    <>
      <Modal
        open={open}
        disableEscapeKeyDown
        aria-labelledby="modal-modal-title"
        aria-describedby="modal-modal-description"
      >
        <Box sx={style}>
          <Typography id="modal-modal-title" variant="h6" component="h2">
            {title}
          </Typography>

          <TextField
            fullWidth
            label="Client ID"
            value={data?.client_id || ''}
            disabled
            margin="normal"
          />
          <TextField
            fullWidth
            margin="normal"
            label="Client Secret"
            type={showPassword ? 'text' : 'password'}
            value={data?.client_secret || ''}
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton
                    aria-label="toggle password visibility"
                    onClick={handleClickShowPassword}
                    onMouseDown={handleMouseDownPassword}
                  >
                    {showPassword ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />
          <Typography variant="body2" color="text.secondary">
            Please save your client secret in a safe place. After closing, it
            will not be shown again.
          </Typography>
          <Button
            variant="contained"
            color="primary"
            onClick={onClose}
            sx={{ mt: 2 }}
          >
            Close
          </Button>
        </Box>
      </Modal>
    </>
  );
};
