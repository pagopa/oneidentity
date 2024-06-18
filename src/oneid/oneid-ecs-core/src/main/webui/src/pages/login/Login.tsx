import { useEffect, useState } from 'react';
import Button from '@mui/material/Button';
import Link from '@mui/material/Link';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Icon from '@mui/material/Icon';
import { Alert, IconButton } from '@mui/material';
import Typography from '@mui/material/Typography';
import { Trans, useTranslation } from 'react-i18next';
import { theme } from '@pagopa/mui-italia';
import Layout from '../../components/Layout';
import SpidIcon from '../../assets/SpidIcon.svg';
import CIEIcon from '../../assets/CIEIcon.svg';
import { ENV } from '../../utils/env';
import { ENABLE_LANDING_REDIRECT } from '../../utils/constants';
import { isPnpg } from '../../utils/utils';
import SpidSelect from './SpidSelect';
import SpidModal from './SpidModal';

type BannerContent = {
  enable: boolean;
  severity: 'warning' | 'error' | 'info' | 'success';
  description: string;
};

export const spidIcon = () => (
  <Icon sx={{ width: '25px', height: '25px' }}>
    <img src={SpidIcon} width="25" height="25" />
  </Icon>
);

export const cieIcon = () => (
  <Icon sx={{ width: '25px', height: '25px' }}>
    <img src={CIEIcon} width="25" height="25" />
  </Icon>
);

const Login = () => {
  const [showIDPS, setShowIDPS] = useState(false);
  const [fromOnboarding, setFromOnboarding] = useState<boolean>();
  const [product, setProduct] = useState<string>('');
  const [bannerContent, setBannerContent] = useState<Array<BannerContent>>();
  const [openSpidModal, setOpenSpidModal] = useState(false);
  const [isPT, setIsPT] = useState(false);

  const mapToArray = (json: { [key: string]: BannerContent }) => {
    const mapped = Object.values(json);
    setBannerContent(mapped as Array<BannerContent>);
  };

  const alertMessage = async (loginBanner: string) => {
    try {
      const response = await fetch(loginBanner);
      const res = await response.json();
      mapToArray(res as any);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    void alertMessage(ENV.JSON_URL.ALERT);
  }, []);

  useEffect(() => {
    const onboardingUrl = new URLSearchParams(window.location.search).get('onSuccess');

    if (onboardingUrl && onboardingUrl.includes('institutionType=PT')) {
      setIsPT(true);
    } else {
      setIsPT(false);
    }

    const onboardingUrlWithoutInstitution = onboardingUrl?.split('?')[0];

    if (onboardingUrl?.includes('onboarding') && !onboardingUrl?.includes('confirm')) {
      setFromOnboarding(true);
      switch (onboardingUrlWithoutInstitution) {
        case '/onboarding/prod-interop':
          setProduct('Interoperabilità');
          break;
        case '/onboarding/prod-io':
          setProduct('App Io');
          break;
        case '/onboarding/prod-io/prod-io-premium':
          setProduct('App Io Premium');
          break;
        case '/onboarding/prod-io-sign':
          setProduct('Firma con Io');
          break;
        case '/onboarding/prod-pn':
          setProduct('SEND - Servizio Notifiche Digitali');
          break;
        case '/onboarding/prod-pagopa':
          setProduct('Piattaforma pagoPA');
          break;
        case '/onboarding/prod-cgn':
          setProduct('Carta Giovani');
          break;
        case '/onboarding/prod-ciban':
          setProduct('Check-IBAN');
          break;
        default:
          setProduct('');
      }
    } else {
      setFromOnboarding(false);
    }
  }, []);

  const { t } = useTranslation();

  const goCIE = () => {
    // storageSpidSelectedOps.write(ENV.SPID_CIE_ENTITY_ID);
    // trackEvent(
    //   'LOGIN_IDP_SELECTED',
    //   {
    //     SPID_IDP_NAME: 'CIE',
    //     SPID_IDP_ID: ENV.SPID_CIE_ENTITY_ID,
    //   },
    //   () =>
    //     window.location.assign(
    //       `${ENV.URL_API.LOGIN}/login?entityID=${ENV.SPID_CIE_ENTITY_ID}&authLevel=SpidL2`
    //     )
    // );
    window.location.assign(
      `${ENV.URL_API.LOGIN}/login?entityID=${ENV.SPID_CIE_ENTITY_ID}&authLevel=SpidL2`
    );
  };

  const goBackToLandingPage = () => {
    window.location.assign(`${ENV.URL_FE.LANDING}`);
  };

  const onBackAction = () => {
    setShowIDPS(false);
  };

  const onLinkClick = () => {
    setShowIDPS(true);
  };

  if (showIDPS) {
    return <SpidSelect onBack={onBackAction} />;
  }

  // const redirectPrivacyLink = () =>
  //   trackEvent('LOGIN_PRIVACY', { SPID_IDP_NAME: 'LOGIN_PRIVACY' }, () =>
  //     window.location.assign(ENV.URL_FOOTER.PRIVACY_DISCLAIMER)
  //   );

  const columnsOccupiedByAlert = 5;

  return (
    <Layout>
      <Grid container direction="column" my={'auto'} alignItems="center">
        <Grid container direction="row" justifyContent="flex-end" mt={8}>
          <Grid item xs={2}>
            {ENABLE_LANDING_REDIRECT && (
              <IconButton
                color="primary"
                sx={{
                  maxWidth: '17.42px',
                  '&:hover': { backgroundColor: 'transparent !important' },
                }}
                onClick={() => goBackToLandingPage()}
              ></IconButton>
            )}
          </Grid>
        </Grid>
        <Grid container item justifyContent="center" mb={isPnpg ? 8 : 0}>
          <Grid item xs={4} maxWidth="100%">
            <Typography
              variant="h3"
              py={1}
              px={0}
              color="textPrimary"
              sx={{
                textAlign: 'center',
              }}
            >
              {fromOnboarding || isPnpg ? t('loginPageFromOnboarding.title') : t('loginPage.title')}
            </Typography>
          </Grid>
        </Grid>

        {!isPnpg && (
          <Grid container item justifyContent="center">
            <Grid item xs={6}>
              <Typography
                variant="body1"
                mb={5}
                color="textPrimary"
                sx={{
                  textAlign: 'center',
                }}
              >
                {fromOnboarding ? (
                  <Trans
                    i18nKey={
                      isPT
                        ? 'loginPageFromOnboarding.descriptionPT'
                        : 'loginPageFromOnboarding.description'
                    }
                    values={{ nomeProdotto: product }}
                    components={{ 1: <br />, 3: <strong /> }}
                  >
                    {isPT
                      ? 'Seleziona la modalità che preferisci per accedere e registrarti <1 /> come Partner tecnologico per il prodotto <3>{{nomeProdotto}}<3/>.'
                      : `Seleziona la modalità di accesso che preferisci e inizia il <1 /> processo di  adesione al prodotto <3>{{nomeProdotto}}<3/>.`}
                  </Trans>
                ) : (
                  t('loginPage.description')
                )}
              </Typography>
            </Grid>
          </Grid>
        )}
        {ENV.ENABLED_SPID && (
          <Grid container justifyContent="center" mb={5}>
            <Grid item>
              <Alert severity="warning">
                {t('loginPage.temporaryLogin.alert')}
                <Link
                  ml={4}
                  sx={{ fontWeight: 'fontWeightBold', cursor: 'pointer', textDecoration: 'none' }}
                  onClick={onLinkClick}
                >
                  {t('loginPage.temporaryLogin.join')}
                </Link>
              </Alert>
            </Grid>
          </Grid>
        )}
        {fromOnboarding &&
          bannerContent &&
          bannerContent.map(
            (bc, index) =>
              bc.enable && (
                <Grid container item justifyContent="center" key={index} mt={2}>
                  <Grid item xs={columnsOccupiedByAlert}>
                    <Box display="flex" justifyContent="center" mb={5}>
                      <Alert severity={bc.severity} sx={{ width: '100%' }}>
                        <Typography textAlign="center">{bc.description}</Typography>
                      </Alert>
                    </Box>
                  </Grid>
                </Grid>
              )
          )}
        <Grid
          container
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
          <SpidModal openSpidModal={openSpidModal} setOpenSpidModal={setOpenSpidModal} />
          <Grid item sx={{ width: '100%' }}>
            <Button
              id="spidButton"
              sx={{
                borderRadius: '4px',
                width: '100%',
                marginBottom: '5px',
              }}
              onClick={() => setOpenSpidModal(true)}
              variant="contained"
              disableElevation
              startIcon={spidIcon()}
            >
              <Typography
                sx={{
                  fontWeight: 'fontWeightMedium',
                  textAlign: 'center',
                  color: theme.palette.primary.contrastText,
                }}
              >
                {t('loginPage.loginBox.spidLogin')}
              </Typography>
            </Button>
          </Grid>
          <Grid item sx={{ width: '100%' }}>
            <Button
              sx={{
                borderRadius: '4px',
                width: '100%',
                marginTop: 2,
              }}
              variant="contained"
              startIcon={cieIcon()}
              onClick={() => goCIE()}
            >
              {t('loginPage.loginBox.cieLogin')}
            </Button>
          </Grid>
        </Grid>
        <Grid container item justifyContent="center">
          <Grid item xs={6}>
            <Typography
              color="textPrimary"
              mt={5}
              px={0}
              sx={{
                textAlign: 'center',
              }}
              component="div"
              variant="body1"
            >
              <Trans i18nKey="loginPage.privacyAndCondition" shouldUnescape>
                Accedendo accetti i
                <Link
                  sx={{
                    cursor: 'pointer',
                    textDecoration: 'none !important',
                    fontWeight: '400',
                    color: 'primary.main',
                  }}
                  onClick={() => {
                    // trackEvent('LOGIN_TOS', { SPID_IDP_NAME: 'LOGIN_TOS' }, () =>
                    //   window.location.assign(ENV.URL_FOOTER.TERMS_AND_CONDITIONS)
                    // );
                  }}
                >
                  {'Termini e condizioni d’uso'}
                </Link>
                del servizio e
                <br />
                confermi di avere letto l&apos;
                <Link
                  sx={{
                    cursor: 'pointer',
                    textDecoration: 'none !important',
                    fontWeight: '400',
                    color: 'primary.main',
                  }}
                  // onClick={redirectPrivacyLink}
                >
                  Informativa Privacy
                </Link>
              </Trans>
            </Typography>
          </Grid>
        </Grid>
      </Grid>
    </Layout>
  );
};

export default Login;
