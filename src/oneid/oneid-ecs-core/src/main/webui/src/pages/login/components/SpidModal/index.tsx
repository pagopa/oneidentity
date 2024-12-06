import { Button, Dialog, Grid, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { IdentityProviders } from '../../../../utils/IDPS';
import { SpidSkeleton } from '../SpidSkeleton';
import { SpidSelection } from '../SpidSelection';

type Props = {
  openSpidModal: boolean;
  setOpenSpidModal: (openDialog: boolean) => void;
  idpList?: IdentityProviders;
  loading?: boolean;
};

export const NoProviders = () => {
  const { t } = useTranslation();

  return (
    <Typography
      variant="caption-semibold"
      color="textPrimary"
      sx={{ textAlign: 'center' }}
      fontSize={16}
    >
      {t('spidSelect.placeholder')}
    </Typography>
  );
};

export const ContentSelection = ({
  idpList,
}: {
  idpList?: IdentityProviders;
}) => {
  const noSpidProvidersFound = !idpList?.identityProviders?.length;

  return noSpidProvidersFound ? (
    <NoProviders />
  ) : (
    <SpidSelection identityProviders={idpList.identityProviders} />
  );
};

const SpidModal = ({
  openSpidModal,
  setOpenSpidModal,
  idpList,
  loading,
}: Props) => {
  const { t } = useTranslation();

  return (
    <Dialog
      role="dialog"
      open={openSpidModal}
      onClose={() => setOpenSpidModal(false)}
      aria-busy={loading}
      aria-live="polite"
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
      {loading ? <SpidSkeleton /> : <ContentSelection idpList={idpList} />}
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
