import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, it, vi } from 'vitest';
import { EidasButton, EidasButtonProps } from './EidasButton';
import { i18nTestSetup } from '../../../../__tests__/i18nTestSetup';

describe('EidasButton', () => {
  const EIDAS_BUTTON_TEXT = 'eIDAS Login';
  const onClickMock = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = (props: Partial<EidasButtonProps> = {}) => {
    i18nTestSetup({
      'loginPage.loginBox.eidasLogin': EIDAS_BUTTON_TEXT,
    });
    render(<EidasButton onClick={onClickMock} {...props} />);
  };

  it('does not render by default', () => {
    renderComponent();
    expect(
      screen.queryByRole('button', { name: EIDAS_BUTTON_TEXT })
    ).not.toBeInTheDocument();
  });

  it('renders with the correct text when visible', () => {
    renderComponent({ visible: true });
    expect(
      screen.getByRole('button', { name: EIDAS_BUTTON_TEXT })
    ).toBeInTheDocument();
  });

  it('displays the eidas icon when visible', () => {
    renderComponent({ visible: true });
    const icon = screen.getByAltText('eIDAS Icon');
    expect(icon).toBeInTheDocument();
    const iconSrc = icon.getAttribute('src');
    expect(iconSrc).toBeTruthy();
    expect(iconSrc).toMatch(/eIDAS|data:image\/svg\+xml/i);
  });

  it('calls the onClick handler when clicked', async () => {
    renderComponent({ visible: true });
    const button = screen.getByRole('button', { name: EIDAS_BUTTON_TEXT });
    await userEvent.click(button);
    expect(onClickMock).toHaveBeenCalledTimes(1);
  });
});
