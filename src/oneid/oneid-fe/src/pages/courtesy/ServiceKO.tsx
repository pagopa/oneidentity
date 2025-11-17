import { IllusUmbrella } from '@pagopa/mui-italia';
import EndingPage from '../../components/EndingPage';
import Layout from '../../components/Layout';
import { Box } from '@mui/material';
import { useTranslation } from 'react-i18next';

export const ServiceKO = () => {
  const { t } = useTranslation();
  const STATIC_URL = 'https://bonuselettrodomestici.it/utente';
  const handleRedirect = () => {
    window.location.assign(STATIC_URL);
  };
  return (
    <Layout hidePreFooter={true}>
      <Box pt={16} />
      <EndingPage
        icon={<IllusUmbrella size={60} />}
        variantTitle="h4"
        variantDescription="body1"
        title={'Qualcosa non va'}
        description={'Il sito non è al momento disponibile. Riprova più tardi.'}
        labelButton={t('loginError.close')}
        onClickButton={handleRedirect}
        variantButton={'contained'}
        haveTwoButtons={false}
      />
      <Box pb={16} />
    </Layout>
  );
};
