import { Container, ContainerProps } from '@mui/material';

export const PageContainer = (props: ContainerProps) => (
  <Container
    maxWidth="md"
    sx={{ py: 4, mx: 'auto', px: { xs: 0, sm: 2, md: 4 } }}
    {...props}
  />
);
