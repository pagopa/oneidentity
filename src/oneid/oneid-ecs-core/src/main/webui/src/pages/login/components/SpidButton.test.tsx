import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import { SpidButton, SpidButtonProps } from './SpidButton';
import { i18nTestSetup } from '../../../__tests__/i18nTestSetup';

const TEST_ID = 'spidButton';

describe('SpidButton Component', () => {
  const onClickMock = vi.fn();

  beforeAll(() => {
    i18nTestSetup({
      'loginPage.loginBox.spidLogin': 'Login with SPID',
    });
  });

  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderComponent = (props: SpidButtonProps) =>
    render(<SpidButton {...props} />);

  it('renders the button with the correct text', () => {
    renderComponent({ loading: false, onClick: onClickMock });

    const button = screen.getByTestId(TEST_ID);
    expect(button).toBeInTheDocument();
    expect(button).toHaveTextContent('Login with SPID');
  });

  it('renders the button in a loading state', () => {
    renderComponent({ loading: true, onClick: onClickMock });

    const button = screen.getByTestId(TEST_ID);
    expect(button).toHaveAttribute('aria-busy', 'true');
    expect(button).toBeDisabled();
  });

  it('triggers the onClick function when clicked', () => {
    renderComponent({ loading: false, onClick: onClickMock });

    const button = screen.getByTestId(TEST_ID);
    fireEvent.click(button);

    expect(onClickMock).toHaveBeenCalledOnce();
  });

  it('displays the SPID icon', () => {
    renderComponent({ loading: false, onClick: onClickMock });

    const icon = screen.getByAltText('SPID Icon');
    expect(icon).toBeInTheDocument();
    expect(icon).toHaveAttribute('src', expect.stringContaining('SpidIcon'));
  });
});
