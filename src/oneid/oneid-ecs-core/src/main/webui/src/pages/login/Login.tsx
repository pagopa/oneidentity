import { useEffect, useState } from 'react';
import Button from '@mui/material/Button';
import Link from '@mui/material/Link';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Icon from '@mui/material/Icon';
import { Alert } from '@mui/material';
import Typography from '@mui/material/Typography';
import { Trans, useTranslation } from 'react-i18next';
import { theme } from '@pagopa/mui-italia';

import Layout from '../../components/Layout';
import SpidIcon from '../../assets/SpidIcon.svg';
import CIEIcon from '../../assets/CIEIcon.svg';
import { ENV } from '../../utils/env';
import { IDP_PLACEHOLDER_IMG } from '../../utils/constants';
import { trackEvent } from '../../services/analyticsService';
import { forwardSearchParams } from '../../utils/utils';
import type { IdentityProvider, IdentityProviders } from '../../utils/IDPS';
import { ImageWithFallback } from '../../components/ImageFallback';
import SpidSelect from './SpidSelect';
import SpidModal from './SpidModal';
import LoadingButton from '@mui/lab/LoadingButton';

type BannerContent = {
  enable: boolean;
  severity: 'warning' | 'error' | 'info' | 'success';
  description: string;
};

type Client = {
  clientID: string;
  friendlyName: string;
  logoUri: string;
  policyUri: string;
  tosUri: string;
};

export const SpidIconWrapper = () => (
  <Icon sx={{ width: '25px', height: '25px' }}>
    <img src={SpidIcon} width="25" height="25" />
  </Icon>
);

export const CieIconWrapper = () => (
  <Icon sx={{ width: '25px', height: '25px' }}>
    <img src={CIEIcon} width="25" height="25" />
  </Icon>
);

export const LinkWrapper = ({
  onClick,
  children,
}: {
  onClick: () => void;
  children?: React.ReactNode;
}) => (
  <Link
    key="termsLink"
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
  const [showIDPS, setShowIDPS] = useState(false);
  const [bannerContent, setBannerContent] = useState<Array<BannerContent>>();
  const [openSpidModal, setOpenSpidModal] = useState(false);
  const [idpList, setIdpList] = useState<IdentityProviders>({
    identityProviders: [],
    richiediSpid: '',
  });
  const [clientData, setClientData] = useState<Client>();

  const mapToArray = (json: Record<string, BannerContent>) => {
    const mapped = Object.values(json);
    setBannerContent(mapped as Array<BannerContent>);
  };

  const alertMessage = async (loginBanner: string) => {
    try {
      const response = await fetch(loginBanner);
      const res = await response.json();
      mapToArray(res);
    } catch (error) {
      console.error(error);
    }
  };

  const getIdpList = async (idpListUrl: string) => {
    try {
      const response = await fetch(idpListUrl);
      const res: Array<IdentityProvider> = await response.json();
      const assetsIDPUrl = ENV.URL_FE.ASSETS + '/idps';
      const rawIDPS = res
        .map((i) => ({
          ...i,
          imageUrl: `${assetsIDPUrl}/${btoa(i.entityID)}.png`,
        }))
        .sort(() => 0.5 - Math.random());
      const IDPS: {
        identityProviders: Array<IdentityProvider>;
        richiediSpid: string;
      } = {
        identityProviders: rawIDPS,
        richiediSpid: 'https://www.spid.gov.it/cos-e-spid/come-attivare-spid/',
      };
      setIdpList(IDPS);
    } catch (error) {
      console.error(error);
    }
  };

  const getClientData = async (clientBaseListUrl: string) => {
    try {
      const query = new URLSearchParams(window.location.search);
      const clientID = query.get('client_id');

      if (clientID && clientID.match(/^[A-Za-z0-9_-]{43}$/)) {
        const clientListUrl = `${clientBaseListUrl}/${clientID}`;
        const response = await fetch(clientListUrl);
        const res: Client = await response.json();
        setClientData(res);
      } else {
        console.warn('no client_id supplied, or not valid 32bit Base64Url');
      }
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    void alertMessage(ENV.JSON_URL.ALERT);
    void getIdpList(ENV.JSON_URL.IDP_LIST);
    void getClientData(ENV.JSON_URL.CLIENT_BASE_URL);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const { t } = useTranslation();

  const goCIE = () => {
    const params = forwardSearchParams(ENV.SPID_CIE_ENTITY_ID);
    const redirectUrl = `${ENV.URL_API.AUTHORIZE}?${params}`;
    trackEvent(
      'LOGIN_IDP_SELECTED',
      {
        SPID_IDP_NAME: 'CIE',
        SPID_IDP_ID: ENV.SPID_CIE_ENTITY_ID,
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
      window.location.assign(
        clientData?.policyUri || ENV.URL_FOOTER.PRIVACY_DISCLAIMER
      )
    );

  const redirectToTOS = () =>
    trackEvent('LOGIN_TOS', { SPID_IDP_NAME: 'LOGIN_TOS' }, () =>
      window.location.assign(
        clientData?.tosUri || ENV.URL_FOOTER.TERMS_AND_CONDITIONS
      )
    );

  if (showIDPS) {
    return <SpidSelect onBack={onBackAction} idpList={idpList} />;
  }

  const columnsOccupiedByAlert = 5;

  return (
    <Layout>
      <Grid container direction="column" my={'auto'} alignItems="center">
        <Grid container item justifyContent="center" mb={0}>
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
              {t('loginPage.title')}
            </Typography>
          </Grid>
        </Grid>

        <Grid container item justifyContent="center">
          <Grid item xs={6}>
            <Typography
              variant="body1"
              color="textPrimary"
              mb={3}
              sx={{
                textAlign: 'center',
              }}
            >
              {t('loginPage.description')}
            </Typography>
          </Grid>
        </Grid>
        {clientData?.logoUri && (
          <Grid
            container
            item
            justifyContent="center"
            textAlign={'center'}
            mb={2}
          >
            <Grid item xs={6}>
              <ImageWithFallback
                style={{
                  width: '100%',
                  maxWidth: '100px',
                  maxHeight: '100px',
                  objectFit: 'cover',
                }}
                src={clientData?.logoUri}
                alt={clientData?.friendlyName}
                placeholder={IDP_PLACEHOLDER_IMG}
              />
            </Grid>
          </Grid>
        )}
        {ENV.ENABLED_SPID_TEMPORARY_SELECT && (
          <Grid container justifyContent="center" mb={5}>
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
        {bannerContent &&
          bannerContent.map(
            (bc, index) =>
              bc.enable && (
                <Grid container item justifyContent="center" key={index} mt={2}>
                  <Grid item xs={columnsOccupiedByAlert}>
                    <Box display="flex" justifyContent="center" mb={5}>
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
            idpList={idpList}
          />
          <Grid item sx={{ width: '100%' }}>
            <LoadingButton
              aria-busy={!!idpList}
              disableElevation
              id="spidButton"
              loading={!!idpList}
              loadingPosition="end"
              onClick={() => setOpenSpidModal(true)}
              startIcon={<SpidIconWrapper />}
              sx={{
                borderRadius: '4px',
                width: '100%',
                marginBottom: '5px',
              }}
              variant="contained"
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
            </LoadingButton>
          </Grid>
          <Grid item sx={{ width: '100%' }}>
            <Button
              sx={{
                borderRadius: '4px',
                width: '100%',
                marginTop: 2,
              }}
              variant="contained"
              startIcon={<CieIconWrapper />}
              onClick={goCIE}
            >
              {t('loginPage.loginBox.cieLogin')}
            </Button>
          </Grid>
        </Grid>
        <Grid container item justifyContent="center">
          <Typography
            color="textPrimary"
            mt={5}
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
                <LinkWrapper key="termsLink" onClick={redirectToTOS} />,
                <LinkWrapper key="privacyLink" onClick={redirectPrivacyLink} />,
              ]}
            />
          </Typography>
        </Grid>
      </Grid>
    </Layout>
  );
};

export default Login;
