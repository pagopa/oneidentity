import {
  IdentityButton,
  type IdentityProviderButtonProps,
} from './IdentityButton';
import CIEIcon from '../../../../assets/CIEIcon.svg';

export type CieButtonProps = IdentityProviderButtonProps;

export const CieButton = ({ onClick, disabled }: CieButtonProps) => (
  <IdentityButton
    onClick={onClick}
    disabled={disabled}
    ariaLabel="CIE Login"
    iconAlt="CIE Icon"
    iconSrc={CIEIcon}
    translationKey="loginPage.loginBox.cieLogin"
  />
);
