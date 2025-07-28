import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
} from '@mui/material';

type ConfirmDialogProps = {
  open: boolean;
  title?: string;
  content?: string;
  onCancel: () => void;
  onConfirm: () => void;
  confirmText?: string;
  cancelText?: string;
};

const ConfirmDialog = ({
  open,
  title = 'Confirm',
  content = 'Are you sure?',
  onCancel,
  onConfirm,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
}: ConfirmDialogProps) => {
  return (
    <Dialog open={open} onClose={onCancel}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Typography>{content}</Typography>
      </DialogContent>
      <DialogActions>
        <Button
          onClick={onCancel}
          color="primary"
          sx={{
            '&:hover': {
              backgroundColor: 'rgba(25, 118, 210, 0.08) !important',
            },
          }}
        >
          {cancelText}
        </Button>
        <Button
          onClick={onConfirm}
          color="error"
          autoFocus
          sx={{
            '&:hover': {
              color: '#D85757 !important',
              backgroundColor: 'rgba(216, 87, 87, 0.1) !important',
            },
          }}
        >
          {confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConfirmDialog;
