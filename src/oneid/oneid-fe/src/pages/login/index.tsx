import { useState } from 'react';
import Link from '@mui/material/Link';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import { Alert } from '@mui/material';
import Typography from '@mui/material/Typography';
import { Trans, useTranslation } from 'react-i18next';
import { ButtonNaked, LangCode, theme } from '@pagopa/mui-italia';

import Layout from '../../components/Layout';
import { ENV } from '../../utils/env';
import { trackEvent } from '../../services/analyticsService';
import { forwardSearchParams } from '../../utils/utils';
import SpidModal from './components/SpidModal';
import { useLoginData } from '../../hooks/useLoginData';
import { SpidButton } from './components/SpidButton';
import { CieButton } from './components/CieButton';
import SpidSelect from './components/SpidSelect';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import i18n from '../../locale';

export const LinkWrapper = ({
  onClick,
  children,
}: {
  onClick: () => void;
  children?: React.ReactNode;
}) => (
  <Link
    sx={{
      cursor: 'pointer',
      textDecoration: 'none !important',
      fontWeight: '400',
      color: 'primary.main',
    }}
    onClick={onClick}
  >
    {children}
  </Link>
);

const Login = () => {
  const [openSpidModal, setOpenSpidModal] = useState(false);
  const [showIDPS, setShowIDPS] = useState(false);

  const { bannerQuery, clientQuery, idpQuery } = useLoginData();
  const { t } = useTranslation();
  const lang: LangCode = i18n.language as LangCode;

  const columnsOccupiedByAlert = 5;

  const goCIE = () => {
    const params = forwardSearchParams(ENV.CIE_ENTITY_ID);
    const redirectUrl = `${ENV.URL_API.AUTHORIZE}?${params}`;
    trackEvent(
      'LOGIN_IDP_SELECTED',
      {
        SPID_IDP_NAME: 'CIE',
        SPID_IDP_ID: ENV.CIE_ENTITY_ID,
        FORWARD_PARAMETERS: params,
      },
      () => window.location.assign(redirectUrl)
    );
  };

  const onBackAction = () => {
    setShowIDPS(false);
  };

  const onLinkClick = () => {
    setShowIDPS(true);
  };

  const redirectPrivacyLink = () =>
    trackEvent('LOGIN_PRIVACY', { SPID_IDP_NAME: 'LOGIN_PRIVACY' }, () =>
      window.open(
        clientQuery.data?.policyUri || ENV.URL_FOOTER.PRIVACY_DISCLAIMER,
        '_blank'
      )
    );

  const redirectToTOS = () =>
    trackEvent('LOGIN_TOS', { SPID_IDP_NAME: 'LOGIN_TOS' }, () =>
      window.open(
        clientQuery.data?.tosUri || ENV.URL_FOOTER.TERMS_AND_CONDITIONS,
        '_blank'
      )
    );

  if (showIDPS) {
    return (
      <SpidSelect
        onBack={onBackAction}
        idpList={idpQuery.data}
        loading={idpQuery.isLoading}
      />
    );
  }

  return (
    <Layout>
      <Box mt={3} ml={3}>
        {clientQuery.data?.backButtonEnabled ? (
          <ButtonNaked
            color="primary"
            sx={{ fontWeight: '700' }}
            onClick={() => window.history.back()}
            size="medium"
            startIcon={<ArrowBackIcon />}
          >
            {t('common.backButtonText')}
          </ButtonNaked>
        ) : (
          <Box pt={3} />
        )}
      </Box>
      <Grid container direction="column" alignItems="center" mt={7}>
        <Grid container item justifyContent="center" mb={0}>
          <Grid item xs={6} maxWidth="100%">
            <Typography
              variant="h3"
              py={1}
              px={0}
              color="textPrimary"
              sx={{
                textAlign: 'center',
              }}
            >
              {clientQuery.data?.localizedContentMap?.[lang]?.title ||
                t('loginPage.title')}
            </Typography>
          </Grid>
        </Grid>

        <Grid container item justifyContent="center" mb={4}>
          <Grid item xs={10}>
            <Typography
              variant="body1"
              color="textPrimary"
              sx={{
                textAlign: 'center',
              }}
            >
              {clientQuery.data?.localizedContentMap?.[lang]?.description ||
                t('loginPage.description')}
            </Typography>
          </Grid>
        </Grid>

        {ENV.ENABLED_SPID_TEMPORARY_SELECT && (
          <Grid container justifyContent="center" mb={4}>
            <Grid item>
              <Alert severity="warning">
                {t('loginPage.temporaryLogin.alert')}
                <Link
                  ml={4}
                  sx={{
                    fontWeight: 'fontWeightBold',
                    cursor: 'pointer',
                    textDecoration: 'none',
                  }}
                  onClick={onLinkClick}
                >
                  {t('loginPage.temporaryLogin.join')}
                </Link>
              </Alert>
            </Grid>
          </Grid>
        )}
        {bannerQuery.isSuccess &&
          bannerQuery.data.map(
            (bc, index) =>
              bc.enable && (
                <Grid container item justifyContent="center" key={index}>
                  <Grid item xs={columnsOccupiedByAlert}>
                    <Box display="flex" justifyContent="center" mb={4}>
                      <Alert severity={bc.severity} sx={{ width: '100%' }}>
                        <Typography textAlign="center">
                          {bc.description}
                        </Typography>
                      </Alert>
                    </Box>
                  </Grid>
                </Grid>
              )
          )}
        <Grid
          container
          // item - it fixes warning in console but breaks container size
          xs={6}
          lg={3}
          xl={3}
          sx={{
            boxShadow:
              '0px 8px 10px -5px rgba(0, 43, 85, 0.1), 0px 16px 24px 2px rgba(0, 43, 85, 0.05), 0px 6px 30px 5px rgba(0, 43, 85, 0.1)',
            borderRadius: '16px',
            p: 4,
            justifyContent: 'center',
            width: '100%',
            maxWidth: '100%',
            [theme.breakpoints.down('md')]: {
              width: '80%',
            },
          }}
        >
          <SpidModal
            openSpidModal={openSpidModal}
            setOpenSpidModal={setOpenSpidModal}
            idpList={idpQuery.data}
            loading={idpQuery.isLoading}
          />
          <Grid item sx={{ width: '100%' }}>
            <SpidButton onClick={() => setOpenSpidModal(true)} />
          </Grid>
          <Grid item sx={{ width: '100%' }}>
            <CieButton onClick={goCIE} />
          </Grid>
        </Grid>
        <Grid container item justifyContent="center" mt={4}>
          <Typography
            color="textPrimary"
            px={0}
            maxWidth={theme.spacing(55)}
            sx={{
              textAlign: 'center',
            }}
            component="div"
            variant="body1"
          >
            <Trans
              i18nKey="loginPage.privacyAndCondition.text"
              values={{
                termsLink: `<0>${t('loginPage.privacyAndCondition.terms')}</0>`,
                privacyLink: `<1>${t('loginPage.privacyAndCondition.privacy')}</1>`,
              }}
              components={[
                <LinkWrapper
                  data-testid="terms-link"
                  key="termsLink"
                  onClick={redirectToTOS}
                />,
                <LinkWrapper
                  data-testid="privacy-link"
                  key="privacyLink"
                  onClick={redirectPrivacyLink}
                />,
              ]}
            />
          </Typography>
        </Grid>
      </Grid>
    </Layout>
  );
};

export default Login;
