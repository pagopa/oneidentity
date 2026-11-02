import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { ENV } from '../../../utils/env';

type Props = {
  open: boolean;
  onClose: () => void;
  onSelect: (entityId: string) => void;
};

const CieModal = ({ open, onClose, onSelect }: Props) => {
  const { t } = useTranslation();
  const cieTestEnvironments = [
    {
      label: 'cieSelect.preproduction',
      entityId:
        'https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO',
      enabled: ENV.CIE_TEST_ENVIRONMENT_ENABLED.PREPRODUCTION,
    },
    {
      label: 'cieSelect.testing',
      entityId:
        'https://collaudo.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO',
      enabled: ENV.CIE_TEST_ENVIRONMENT_ENABLED.TESTING,
    },
  ];

  return (
    <Dialog open={open} onClose={onClose} aria-labelledby="cie-select-title">
      <DialogTitle id="cie-select-title">
        {t('cieSelect.modalTitle')}
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2}>
          {cieTestEnvironments.map(({ label, entityId, enabled }) => (
            <Button
              key={entityId}
              variant="outlined"
              disabled={!enabled}
              onClick={() => onSelect(entityId)}
            >
              {t(label)}
            </Button>
          ))}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{t('spidSelect.cancelButton')}</Button>
      </DialogActions>
    </Dialog>
  );
};

export default CieModal;
