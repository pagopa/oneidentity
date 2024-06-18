import { Button, Dialog, Grid, Icon, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { IDPS, IdentityProvider } from '../../utils/IDPS';

type Props = {
  openSpidModal: boolean;
  setOpenSpidModal: (openDialog: boolean) => void;
};

const SpidModal = ({ openSpidModal, setOpenSpidModal }: Props) => {
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

  return (
    <>
      <Dialog open={openSpidModal}>
        <Typography
          fontSize={24}
          fontWeight={600}
          py={4}
          px={2}
          color="textPrimary"
          sx={{
            textAlign: 'center',
          }}
        >
          {t('spidSelect.modalTitle')}
        </Typography>
        <Grid item maxWidth={375}>
          <Grid container direction="row" justifyItems="center">
            {IDPS.identityProviders.map((IDP, i) => (
              <Grid
                item
                key={IDP.entityId}
                xs={6}
                p={1}
                textAlign={i % 2 === 0 ? 'right' : 'left'}
                sx={{ minWidth: '100px' }}
              >
                <Button
                  onClick={() => getSPID(IDP)}
                  sx={{ backgroundColor: 'background.default', alignItems: 'center' }}
                  aria-label={IDP.name}
                  id={IDP.entityId}
                >
                  <Icon
                    sx={{ width: '100px', height: '48px', display: 'flex', alignItems: ' center' }}
                  >
                    <img width="100px" src={IDP.imageUrl} alt={IDP.name} />
                  </Icon>
                </Button>
              </Grid>
            ))}
          </Grid>
        </Grid>
        <Grid p={4}>
          <Button onClick={() => setOpenSpidModal(false)} fullWidth variant="outlined">
            {t('spidSelect.cancelButton')}
          </Button>
        </Grid>
      </Dialog>
    </>
  );
};

export default SpidModal;
