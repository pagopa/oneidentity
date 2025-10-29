import { SxProps, Theme } from '@mui/material';

export const tooltipLinkSx: SxProps<Theme> = {
  color: 'secondary.main',
  textDecoration: 'underline',
  '&:hover': {
    color: 'secondary.light',
  },
};
