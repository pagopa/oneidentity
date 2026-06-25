import {
  IdentityButton,
  type IdentityProviderButtonProps,
} from './IdentityButton';
import EIDASIcon from '../../../../assets/eIDAS.svg';

export type EidasButtonProps = IdentityProviderButtonProps & {
  visible?: boolean;
};

export const EidasButton = ({
  onClick,
  disabled,
  visible = false,
}: EidasButtonProps) => {
  if (!visible) {
    return null;
  }

  return (
    <IdentityButton
      onClick={onClick}
      disabled={disabled}
      ariaLabel="eIDAS Login"
      iconAlt="eIDAS Icon"
      iconSrc={EIDASIcon}
      translationKey="loginPage.loginBox.eidasLogin"
    />
  );
};
