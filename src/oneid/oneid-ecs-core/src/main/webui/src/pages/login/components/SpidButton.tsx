import LoadingButton from '@mui/lab/LoadingButton';
import SpidIcon from '../../../assets/SpidIcon.svg';
import Icon from '@mui/material/Icon';
import Typography from '@mui/material/Typography';
import { theme } from '@pagopa/mui-italia/dist/theme';
import { useTranslation } from 'react-i18next';

export const SpidIconWrapper = () => (
  <Icon sx={{ width: '25px', height: '25px' }}>
    <img src={SpidIcon} width="25" height="25" alt="SPID Icon" />
  </Icon>
);

export type SpidButtonProps = {
  loading: boolean;
  onClick: () => void;
};

export const SpidButton = ({ loading, onClick }: SpidButtonProps) => {
  const { t } = useTranslation();

  return (
    <LoadingButton
      aria-busy={loading}
      disableElevation
      id="spidButton"
      data-testid="spidButton"
      loading={loading}
      loadingPosition="end"
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
