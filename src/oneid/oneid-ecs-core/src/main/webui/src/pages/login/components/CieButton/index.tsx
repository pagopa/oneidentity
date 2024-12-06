import Icon from '@mui/material/Icon';
import { useTranslation } from 'react-i18next';
import Button from '@mui/material/Button';
import CIEIcon from '../../../../assets/CIEIcon.svg';

export type CieButtonProps = {
  onClick: () => void;
};

export const CieIconWrapper = () => (
  <Icon sx={{ width: '25px', height: '25px' }}>
    <img src={CIEIcon} width="25" height="25" alt="CIE Icon" />
  </Icon>
);

export const CieButton = ({ onClick }: CieButtonProps) => {
  const { t } = useTranslation();

  return (
    <Button
      sx={{
        borderRadius: '4px',
        width: '100%',
        marginTop: 2,
      }}
      variant="contained"
      startIcon={<CieIconWrapper />}
      onClick={onClick}
    >
      {t('loginPage.loginBox.cieLogin')}
    </Button>
  );
};
