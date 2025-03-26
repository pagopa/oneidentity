import { Box } from '@mui/material';
import Header from './Header';
import { ENV } from '../utils/env';
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
      <Header
        withSecondHeader
        enableAssistanceButton={ENV.CURRENT_ENV !== 'UAT'}
        assistanceEmail={
          ENV.ASSISTANCE.ENABLE ? ENV.ASSISTANCE.EMAIL : undefined
        }
        enableLogin={false}
        loggedUser={false}
      />

      {children}
      <Box mt={10}>
        <Footer loggedUser={false} productsJsonUrl={PRODUCTS_URL} />
      </Box>
    </Box>
  );
};

export default Layout;
