import { Button, Dialog, Grid, Icon, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';

import { IdentityProvider, IdentityProviders } from '../../../utils/IDPS';
import { trackEvent } from '../../../services/analyticsService';
import { forwardSearchParams } from '../../../utils/utils';
import { ENV } from '../../../utils/env';
import { ImageWithFallback } from '../../../components/ImageFallback';
import { IDP_PLACEHOLDER_IMG } from '../../../utils/constants';

type Props = {
  openSpidModal: boolean;
  setOpenSpidModal: (openDialog: boolean) => void;
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

const IdpListSelection = ({
  identityProviders,
}: {
  identityProviders: Array<IdentityProvider>;
}) =>
  identityProviders?.map((IDP, i) => (
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
  ));

const SpidModal = ({ openSpidModal, setOpenSpidModal, idpList }: Props) => {
  const { t } = useTranslation();

  return (
    <Dialog
      role="dialog"
      open={openSpidModal}
      onClose={() => setOpenSpidModal(false)}
    >
      <Typography
        fontSize={24}
        fontWeight={600}
        py={4}
        px={2}
        color="textPrimary"
        sx={{ textAlign: 'center' }}
      >
        {t('spidSelect.modalTitle')}
      </Typography>
      {idpList?.identityProviders?.length ? (
        <Grid item maxWidth={375}>
          <Grid container direction="row" justifyItems="center">
            <IdpListSelection identityProviders={idpList.identityProviders} />
          </Grid>
        </Grid>
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
      <Grid p={4}>
        <Button
          onClick={() => setOpenSpidModal(false)}
          fullWidth
          variant="outlined"
          data-testid="close-button"
        >
          {t('spidSelect.cancelButton')}
        </Button>
      </Grid>
    </Dialog>
  );
};

export default SpidModal;
