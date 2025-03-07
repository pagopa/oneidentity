import { Box } from '@mui/material';
import Header from './Header';
import { ENV } from '../utils/env';
import Footer from './footer/Footer';

type Props = {
  children: React.ReactNode;
};
const withSecondHeader = false;
const Layout = ({ children }: Props) => (
  <Box
    sx={{
      display: 'flex',
      flexDirection: 'column',
      minHeight: '100vh',
    }}
  >
    <Header
      withSecondHeader={withSecondHeader ?? false}
      enableAssistanceButton={ENV.CURRENT_ENV !== 'UAT'}
      assistanceEmail={ENV.ASSISTANCE.ENABLE ? ENV.ASSISTANCE.EMAIL : undefined}
      enableLogin={false}
      loggedUser={false}
      onDocumentationClick={() => window.open(ENV.URL_DOCUMENTATION, '_blank')}
    />
    {children}
    <Box mt={16}>
      <Footer
        loggedUser={false}
        productsJsonUrl={ENV.JSON_URL.CLIENT_BASE_URL}
      />
    </Box>
  </Box>
);

export default Layout;
