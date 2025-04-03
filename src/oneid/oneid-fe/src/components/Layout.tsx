import { Box } from '@mui/material';
import Header from './Header';
import Footer from './footer/Footer';
import { PRODUCTS_URL } from '../utils/constants';

type Props = {
  children: React.ReactNode;
};

const Layout = ({ children }: Props) => {
  return (
    <Box
      bgcolor={'#F5F5F5'}
      margin={0}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
      }}
    >
      <Header withSecondHeader />

      {children}
      <Box mt={10}>
        <Footer productsJsonUrl={PRODUCTS_URL} />
      </Box>
    </Box>
  );
};

export default Layout;
