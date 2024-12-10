import LoadingButton from '@mui/lab/LoadingButton';
import Icon from '@mui/material/Icon';
import Typography from '@mui/material/Typography';
import { theme } from '@pagopa/mui-italia/dist/theme';
import { useTranslation } from 'react-i18next';
import SpidIcon from '../../../../assets/SpidIcon.svg';

export const SpidIconWrapper = () => (
  <Icon sx={{ width: '25px', height: '25px' }}>
    <img src={SpidIcon} width="25" height="25" alt="SPID Icon" />
  </Icon>
);

export type SpidButtonProps = {
  onClick: () => void;
};

export const SpidButton = ({ onClick }: SpidButtonProps) => {
  const { t } = useTranslation();

  return (
    <LoadingButton
      disableElevation
      id="spidButton"
      data-testid="spidButton"
      onClick={onClick}
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
  );
};
