import { Fragment } from 'react';
import ClearOutlinedIcon from '@mui/icons-material/ClearOutlined';
import Icon from '@mui/material/Icon';
import { IconButton } from '@mui/material';
import Grid from '@mui/material/Grid';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import { useTranslation } from 'react-i18next';

import { IdentityProvider, IdentityProviders } from '../../utils/IDPS';
import SpidBig from '../../assets/spid_big.svg';
import { ENV } from '../../utils/env';
import {
  ENABLE_LANDING_REDIRECT,
  IDP_PLACEHOLDER_IMG,
} from '../../utils/constants';
import { trackEvent } from '../../services/analyticsService';
import { forwardSearchParams } from '../../utils/utils';
import { ImageWithFallback } from '../../components/ImageFallback';

type Props = {
  onBack: () => void;
  idpList: IdentityProviders;
};

export const getSPID = (IDP: IdentityProvider) => {
  const params = forwardSearchParams(IDP.entityID);
  const redirectUrl = `${ENV.URL_API.AUTHORIZE}?${params}`;
  trackEvent(
    'LOGIN_IDP_SELECTED',
    {
      SPID_IDP_NAME: IDP.name,
      SPID_IDP_ID: IDP.entityID,
      FORWARD_PARAMETERS: params,
    },
    () => window.location.assign(redirectUrl)
  );
};

export const SpidList = ({ idpList }: { idpList: IdentityProviders }) =>
  idpList.identityProviders.map((IDP, i) => (
    <Grid
      item
      key={IDP.entityID}
      xs={6}
      textAlign={i % 2 === 0 ? 'right' : 'left'}
      sx={{ minWidth: '100px' }}
    >
      <Button
        onClick={() => getSPID(IDP)}
        sx={{ width: '100px', padding: '0' }}
        aria-label={IDP.name}
        id={IDP.entityID}
      >
        <Icon sx={{ width: '100px', height: '48px' }}>
          <ImageWithFallback
            width="100px"
            src={IDP.imageUrl}
            alt={IDP.name}
            placeholder={IDP_PLACEHOLDER_IMG}
          />
        </Icon>
      </Button>
    </Grid>
  ));

const SpidSelect = ({ onBack, idpList }: Props) => {
  const { t } = useTranslation();
  const goBackToLandingPage = () => {
    window.location.assign(`${ENV.URL_FE.LOGIN}`);
  };

  return (
    <Fragment>
      <Grid container direction="column">
        <Grid
          container
          direction="row"
          justifyContent="space-around"
          mt={3}
          mb={5}
        >
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
                onClick={goBackToLandingPage}
                aria-label={t('spidSelect.closeButton')}
              >
                <ClearOutlinedIcon />
              </IconButton>
            </Grid>
          )}
        </Grid>
        <Grid
          container
          direction="column"
          justifyContent="center"
          alignItems="center"
          spacing="10"
        >
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
              {idpList?.identityProviders?.length ? (
                <SpidList idpList={idpList} />
              ) : (
                <Typography
                  variant="caption-semibold"
                  color="textPrimary"
                  sx={{ textAlign: 'center' }}
                  fontSize={16}
                >
                  {t('spidSelect.placeholder')}
                </Typography>
              )}
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

export default SpidSelect;
