import { Fragment } from 'react';
import ClearOutlinedIcon from '@mui/icons-material/ClearOutlined';
import Icon from '@mui/material/Icon';
import { IconButton } from '@mui/material';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import { useTranslation } from 'react-i18next';
import { IdentityProvider, IDPS } from '../../utils/IDPS';
import SpidBig from '../../assets/spid_big.svg';
import { ENV } from '../../utils/env';
import { ENABLE_LANDING_REDIRECT } from '../../utils/constants';

const Login = ({ onBack }: { onBack: () => void }) => {
  const { t } = useTranslation();
  const getSPID = (IDP: IdentityProvider) => {
    // storageSpidSelectedOps.write(IDP.entityId);
    const redirectUrl = `/oidc/authorize?idp=${IDP.entityId}&client_id=90349&response_type=CODE&redirect_uri=http://localhost:8080`;
    // trackEvent(
    //   'LOGIN_IDP_SELECTED',
    //   {
    //     SPID_IDP_NAME: IDP.name,
    //     SPID_IDP_ID: IDP.entityId,
    //   },
    //   () => window.location.assign(redirectUrl)
    // );
    window.location.assign(redirectUrl);
  };
  const goBackToLandingPage = () => {
    window.location.assign(`${ENV.URL_FE.LANDING}`);
  };

  return (
    <Fragment>
      <Grid container direction="column">
        <Grid container direction="row" justifyContent="space-around" mt={3} mb={5}>
          <Grid item xs={2} display="flex" justifyContent="center">
            <img src={SpidBig} />
          </Grid>
          {ENABLE_LANDING_REDIRECT && (
            <Grid item xs={1} sx={{ textAlign: 'right' }}>
              <IconButton
                color="primary"
                sx={{
                  maxWidth: '17.42px',
                  '&:hover': { backgroundColor: 'transparent !important' },
                }}
                onClick={() => goBackToLandingPage()}
                aria-label={t('spidSelect.closeButton')}
              >
                <ClearOutlinedIcon />
              </IconButton>
            </Grid>
          )}
        </Grid>
        <Grid container direction="column" justifyContent="center" alignItems="center" spacing="10">
          <Grid item>
            <Typography
              pb={5}
              px={0}
              color="textPrimary"
              variant="h4"
              sx={{
                textAlign: 'center',
              }}
              component="div"
            >
              {t('spidSelect.title')}
            </Typography>
          </Grid>
          <Grid item pb={5}>
            <Grid container direction="row" justifyItems="center" spacing={2}>
              {IDPS.identityProviders.map((IDP, i) => (
                <Grid
                  item
                  key={IDP.entityId}
                  xs={6}
                  textAlign={i % 2 === 0 ? 'right' : 'left'}
                  sx={{ minWidth: '100px' }}
                >
                  <Button
                    onClick={() => getSPID(IDP)}
                    sx={{ width: '100px', padding: '0' }}
                    aria-label={IDP.name}
                    id={IDP.entityId}
                  >
                    <Icon sx={{ width: '100px', height: '48px' }}>
                      <img width="100px" src={IDP.imageUrl} alt={IDP.name} />
                    </Icon>
                  </Button>
                </Grid>
              ))}
            </Grid>
          </Grid>
          <Grid item>
            <Button
              type="submit"
              variant="outlined"
              sx={{
                borderRadius: '4px',
                width: '328px',
                height: '50px',
              }}
              onClick={onBack}
            >
              {t('spidSelect.cancelButton')}
            </Button>
          </Grid>
        </Grid>
      </Grid>
    </Fragment>
  );
};

export default Login;
