import { Box } from '@mui/material';

type Props = {
  children: any;
};

const Layout = ({ children }: Props) => (
  <Box
    sx={{
      display: 'flex',
      flexDirection: 'column',
      minHeight: '100vh',
    }}
  >
    {children}
    <Box mt={16} />
  </Box>
);

export default Layout;
