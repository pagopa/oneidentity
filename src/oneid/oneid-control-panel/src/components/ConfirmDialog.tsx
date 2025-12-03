import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  ButtonProps,
} from '@mui/material';

type ConfirmDialogProps = {
  open: boolean;
  title?: string;
  content?: string;
  onCancel: () => void;
  onConfirm: () => void;
  confirmText?: string;
  cancelText?: string;
  confirmButtonProps?: Partial<ButtonProps>;
  cancelButtonProps?: Partial<ButtonProps>;
};

const ConfirmDialog = ({
  open,
  title = 'Confirm',
  content = 'Are you sure?',
  onCancel,
  onConfirm,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  confirmButtonProps,
  cancelButtonProps,
}: ConfirmDialogProps) => {
  return (
    <Dialog open={open} onClose={onCancel}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Typography sx={{ whiteSpace: 'pre-line' }}>{content}</Typography>
      </DialogContent>
      <DialogActions>
        <Button
          onClick={onCancel}
          color={cancelButtonProps?.color ?? 'primary'}
          variant={cancelButtonProps?.variant ?? 'text'}
          sx={
            cancelButtonProps?.sx ??
            ((theme) => ({
              '&:hover': {
                backgroundColor: `${theme.palette.primary.dark}1A !important`,
              },
            }))
          }
        >
          {cancelText}
        </Button>
        <Button
          onClick={onConfirm}
          color={confirmButtonProps?.color ?? 'primary'}
          variant={confirmButtonProps?.variant ?? 'text'}
          autoFocus
          sx={confirmButtonProps?.sx}
        >
          {confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConfirmDialog;
