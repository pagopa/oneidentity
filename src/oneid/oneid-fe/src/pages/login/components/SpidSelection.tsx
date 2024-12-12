import { Button, Grid, Icon } from '@mui/material';
import { ImageWithFallback } from '../../../components/ImageFallback';
import { IDP_PLACEHOLDER_IMG } from '../../../utils/constants';
import { IdentityProvider } from '../../../utils/IDPS';
import { trackEvent } from '../../../services/analyticsService';
import { ENV } from '../../../utils/env';
import { forwardSearchParams } from '../../../utils/utils';

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

export const SpidSelection = ({
  identityProviders,
}: {
  identityProviders: Array<IdentityProvider>;
}) => (
  <Grid item maxWidth={375}>
    <Grid container direction="row" justifyItems="center">
      {identityProviders?.map((IDP, i) => (
        <Grid
          item
          key={IDP.entityID}
          xs={6}
          p={1}
          textAlign={i % 2 === 0 ? 'right' : 'left'}
          sx={{ minWidth: '100px' }}
        >
          <Button
            onClick={() => getSPID(IDP)}
            sx={{
              backgroundColor: 'background.default',
              alignItems: 'center',
            }}
            aria-label={IDP.name}
            id={IDP.entityID}
            data-testid={`idp-button-${IDP.entityID}`}
          >
            <Icon
              sx={{
                width: '100px',
                height: '48px',
                display: 'flex',
                alignItems: ' center',
              }}
            >
              <ImageWithFallback
                width="100px"
                src={IDP.imageUrl}
                alt={IDP.name}
                placeholder={IDP_PLACEHOLDER_IMG}
              />
            </Icon>
          </Button>
        </Grid>
      ))}
    </Grid>
  </Grid>
);
