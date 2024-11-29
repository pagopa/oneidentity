import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, vi } from 'vitest';
import { CieButton, CieButtonProps } from './CieButton';
import { i18nTestSetup } from '../../../__tests__/i18nTestSetup';

describe('CieButton', () => {
  const BUTTON_TEXT = 'CIE Login';
  const onClickMock = vi.fn();

  const renderComponent = (props: Partial<CieButtonProps> = {}) => {
    i18nTestSetup({ 'loginPage.loginBox.cieLogin': BUTTON_TEXT });
    render(<CieButton onClick={onClickMock} {...props} />);
  };

  it('renders the button with the correct text', () => {
    renderComponent();
    expect(
      screen.getByRole('button', { name: BUTTON_TEXT })
    ).toBeInTheDocument();
  });

  it('displays the CIE icon', () => {
    renderComponent();
    const icon = screen.getByAltText('CIE Icon');
    expect(icon).toBeInTheDocument();
    expect(icon).toHaveAttribute('src', expect.stringContaining('CIEIcon'));
  });

  it('calls the onClick handler when clicked', async () => {
    renderComponent();
    const button = screen.getByRole('button', { name: BUTTON_TEXT });
    await userEvent.click(button);
    expect(onClickMock).toHaveBeenCalledTimes(1);
  });
});
