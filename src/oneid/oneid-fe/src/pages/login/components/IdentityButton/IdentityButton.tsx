import Button from '@mui/material/Button';
import Icon from '@mui/material/Icon';
import { useTranslation } from 'react-i18next';

export type IdentityButtonProps = {
  onClick: () => void;
  disabled?: boolean;
  ariaLabel: string;
  iconAlt: string;
  iconSrc: string;
  translationKey: string;
};

export type IdentityProviderButtonProps = Pick<
  IdentityButtonProps,
  'onClick' | 'disabled'
>;

type IdentityIconWrapperProps = {
  alt: string;
  src: string;
};

export const IdentityIconWrapper = ({ alt, src }: IdentityIconWrapperProps) => (
  <Icon sx={{ width: '25px', height: '25px' }}>
    <img src={src} width="25" height="25" alt={alt} />
  </Icon>
);

export const IdentityButton = ({
  onClick,
  disabled,
  ariaLabel,
  iconAlt,
  iconSrc,
  translationKey,
}: IdentityButtonProps) => {
  const { t } = useTranslation();

  return (
    <Button
      sx={{
        borderRadius: '4px',
        width: '100%',
        marginTop: 2,
      }}
      variant="contained"
      startIcon={<IdentityIconWrapper alt={iconAlt} src={iconSrc} />}
      onClick={onClick}
      aria-label={ariaLabel}
      disabled={disabled}
    >
      {t(translationKey)}
    </Button>
  );
};
