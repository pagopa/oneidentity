import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, it, vi } from 'vitest';
import { CieButton, CieButtonProps } from './CieButton';
import { i18nTestSetup } from '../../../../__tests__/i18nTestSetup';

describe('CieButton', () => {
  const CIE_BUTTON_TEXT = 'CIE Login';
  const onClickMock = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = (props: Partial<CieButtonProps> = {}) => {
    i18nTestSetup({
      'loginPage.loginBox.cieLogin': CIE_BUTTON_TEXT,
    });
    render(<CieButton onClick={onClickMock} {...props} />);
  };

  it('renders the cie button with the correct text', () => {
    renderComponent();
    expect(
      screen.getByRole('button', { name: CIE_BUTTON_TEXT })
    ).toBeInTheDocument();
  });

  it('displays the CIE icon', () => {
    renderComponent();
    const icon = screen.getByAltText('CIE Icon');
    expect(icon).toBeInTheDocument();
    expect(icon).toHaveAttribute('src', expect.stringContaining('CIEIcon'));
  });

  it('calls the cie onClick handler when clicked', async () => {
    renderComponent();
    const button = screen.getByRole('button', { name: CIE_BUTTON_TEXT });
    await userEvent.click(button);
    expect(onClickMock).toHaveBeenCalledTimes(1);
  });
});
