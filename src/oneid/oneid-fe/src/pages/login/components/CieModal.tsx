import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
} from '@mui/material';
import { useTranslation } from 'react-i18next';

type Props = {
  open: boolean;
  onClose: () => void;
  onSelect: (entityId: string) => void;
};

const CIE_TEST_ENVIRONMENTS = [
  {
    label: 'cieSelect.preproduction',
    entityId:
      'https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO',
  },
  {
    label: 'cieSelect.testing',
    entityId:
      'https://collaudo.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO',
  },
];

const CieModal = ({ open, onClose, onSelect }: Props) => {
  const { t } = useTranslation();

  return (
    <Dialog open={open} onClose={onClose} aria-labelledby="cie-select-title">
      <DialogTitle id="cie-select-title">
        {t('cieSelect.modalTitle')}
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2}>
          {CIE_TEST_ENVIRONMENTS.map(({ label, entityId }) => (
            <Button
              key={entityId}
              variant="outlined"
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
