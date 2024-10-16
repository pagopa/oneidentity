import { Modal, Typography, useTheme } from '@mui/material';
import { Box } from '@mui/system';
import MDSpinner from 'react-md-spinner';

type LoadingOverlayProps = {
  loadingText: string;
};

export function LoadingOverlay({ loadingText }: LoadingOverlayProps) {
  const theme = useTheme();

  return (
    <Modal
      open={true}
      aria-labelledby="modal-modal-title"
      aria-describedby="modal-modal-description"
    >
      <Box
        sx={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: 400,
          bgcolor: 'background.paper',
          boxShadow: 24,
          p: 4,
          textAlign: 'center',
        }}
      >
        <MDSpinner singleColor={theme.palette.primary.main} />
        {loadingText && <Typography>{loadingText}</Typography>}
      </Box>
    </Modal>
  );
}
