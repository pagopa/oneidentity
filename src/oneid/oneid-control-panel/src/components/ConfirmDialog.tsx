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
          sx={(theme) => ({
            '&:hover': {
              backgroundColor: `${theme.palette.primary.dark}1A !important`,
            },
          })}
        >
          {cancelText}
        </Button>
        <Button
          onClick={onConfirm}
          color="error"
          autoFocus
          sx={(theme) => ({
            '&:hover': {
              color: `${theme.palette.error.dark} !important`,
              backgroundColor: `${theme.palette.error.dark}1A !important`,
            },
          })}
        >
          {confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConfirmDialog;
