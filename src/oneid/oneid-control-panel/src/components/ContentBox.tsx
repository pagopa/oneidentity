import { Box, BoxProps, Paper, PaperProps } from '@mui/material';

type ContentBoxProps = {
  paperProps?: PaperProps;
  boxProps?: BoxProps;
  children?: React.ReactNode;
};

export const ContentBox = ({
  paperProps,
  boxProps,
  children,
}: ContentBoxProps) => (
  <Paper
    elevation={2}
    sx={{ borderRadius: 3, overflow: 'hidden' }}
    {...paperProps}
  >
    <Box p={{ xs: 3, md: 4 }} {...boxProps}>
      {children}
    </Box>
  </Paper>
);
