import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import DrawerNavLeft from './DrawerNav';
import { ENV } from '../utils/env';
import { vi } from 'vitest';

vi.mock('../utils/env', () => ({
  ENV: {
    CURRENT_ENV: 'dev',
  },
}));

describe('DrawerNavLeft', () => {
  const CUSTOMIZE_UI_TEXT = 'Customize UI';

  const renderComponent = (clientId?: string, isAuthenticated?: boolean) =>
    render(
      <BrowserRouter>
        <DrawerNavLeft
          clientId={clientId}
          appBarHeight={64}
          isAuthenticated={isAuthenticated}
        />
      </BrowserRouter>
    );

  it('should always show the "Register" link', () => {
    renderComponent();
    expect(screen.getByText('Register')).toBeInTheDocument();
  });

  it('should show the "Customize UI" link when a clientId and isAuthenticated are provided', () => {
    renderComponent('test-client-id', true);
    expect(screen.getByText(CUSTOMIZE_UI_TEXT)).toBeInTheDocument();
  });

  it('should not show the "Customize UI" link when no clientId is provided', () => {
    renderComponent(undefined, true);
    expect(screen.queryByText(CUSTOMIZE_UI_TEXT)).not.toBeInTheDocument();
  });

  it('should not show the "Customize UI" link when not authenticated', () => {
    renderComponent('test-client-id', false);
    expect(screen.queryByText(CUSTOMIZE_UI_TEXT)).not.toBeInTheDocument();
  });

  it('should show the "Manage Users" link when the environment is not prod', () => {
    renderComponent();
    expect(screen.getByText('Manage Users')).toBeInTheDocument();
  });

  it('should not show the "Manage Users" link when the environment is prod', () => {
    // eslint-disable-next-line functional/immutable-data
    ENV.CURRENT_ENV = 'prod';
    renderComponent();
    expect(screen.queryByText('Manage Users')).not.toBeInTheDocument();
  });
});
