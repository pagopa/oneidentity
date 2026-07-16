import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, it, vi } from 'vitest';
import { IdentityButton, IdentityButtonProps } from './IdentityButton';
import { i18nTestSetup } from '../../../../__tests__/i18nTestSetup';

describe('IdentityButton', () => {
  const BUTTON_TEXT = 'Identity Login';
  const ICON_ALT = 'Identity Icon';
  const ICON_SRC = '/identity.svg';
  const onClickMock = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = (props: Partial<IdentityButtonProps> = {}) => {
    i18nTestSetup({
      'loginPage.loginBox.identityLogin': BUTTON_TEXT,
    });
    render(
      <IdentityButton
        onClick={onClickMock}
        ariaLabel="Identity Login"
        iconAlt={ICON_ALT}
        iconSrc={ICON_SRC}
        translationKey="loginPage.loginBox.identityLogin"
        {...props}
      />
    );
  };

  it('renders the translated text', () => {
    renderComponent();
    expect(
      screen.getByRole('button', { name: BUTTON_TEXT })
    ).toBeInTheDocument();
  });

  it('renders the provided icon', () => {
    renderComponent();
    const icon = screen.getByAltText(ICON_ALT);
    expect(icon).toBeInTheDocument();
    expect(icon).toHaveAttribute('src', ICON_SRC);
  });

  it('calls the onClick handler when clicked', async () => {
    renderComponent();
    const button = screen.getByRole('button', { name: BUTTON_TEXT });
    await userEvent.click(button);
    expect(onClickMock).toHaveBeenCalledTimes(1);
  });

  it('renders as disabled when disabled', () => {
    renderComponent({ disabled: true });
    const button = screen.getByRole('button', { name: BUTTON_TEXT });
    expect(button).toBeDisabled();
  });
});
