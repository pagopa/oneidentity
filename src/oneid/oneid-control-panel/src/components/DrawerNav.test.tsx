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

const clientIdTest = 'test-client-id';

describe('DrawerNavLeft', () => {
  const CUSTOMIZE_UI_TEXT = 'Customize UI';
  const MANAGE_USERS_TEXT = 'Manage Users';

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
    renderComponent(clientIdTest, true);
    expect(screen.getByText(CUSTOMIZE_UI_TEXT)).toBeInTheDocument();
  });

  it('should not show the "Customize UI" link when no clientId is provided', () => {
    renderComponent(undefined, true);
    expect(screen.queryByText(CUSTOMIZE_UI_TEXT)).not.toBeInTheDocument();
  });

  it('should not show the "Customize UI" link when not authenticated', () => {
    renderComponent(clientIdTest, false);
    expect(screen.queryByText(CUSTOMIZE_UI_TEXT)).not.toBeInTheDocument();
  });

  it('should show the "Manage Users" link when the environment is not prod', () => {
    renderComponent(clientIdTest, true);
    expect(screen.getByText(MANAGE_USERS_TEXT)).toBeInTheDocument();
  });

  it('should not show the "Manage Users" link when no clientId is provided', () => {
    renderComponent(undefined, true);
    expect(screen.queryByText(MANAGE_USERS_TEXT)).not.toBeInTheDocument();
  });

  it('should not show the "Manage Users" link when not authenticated', () => {
    renderComponent(clientIdTest, false);
    expect(screen.queryByText(MANAGE_USERS_TEXT)).not.toBeInTheDocument();
  });

  it('should not show the "Manage Users" link when the environment is prod', () => {
    // eslint-disable-next-line functional/immutable-data
    ENV.CURRENT_ENV = 'prod';
    renderComponent();
    expect(screen.queryByText('Manage Users')).not.toBeInTheDocument();
  });
});
