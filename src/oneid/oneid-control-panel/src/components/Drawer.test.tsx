import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import PersistentDrawerLeft from './Drawer';
import { ENV } from '../utils/env';
import { vi } from 'vitest';

vi.mock('../utils/env', () => ({
  ENV: {
    CURRENT_ENV: 'dev',
  },
}));

describe('PersistentDrawerLeft', () => {
  const handleDrawerClose = vi.fn();

  const renderComponent = (open: boolean, clientId?: string) =>
    render(
      <BrowserRouter>
        <PersistentDrawerLeft
          open={open}
          handleDrawerClose={handleDrawerClose}
          clientId={clientId}
        />
      </BrowserRouter>
    );

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should render the drawer when open is true', () => {
    renderComponent(true);
    expect(screen.getByText('Register')).toBeVisible();
  });

  it('should not render the drawer when open is false', () => {
    renderComponent(false);
    expect(screen.queryByText('Register')).not.toBeVisible();
  });

  it('should show the "Customize UI" link when a clientId is provided', () => {
    renderComponent(true, 'test-client-id');
    expect(screen.getByText('Customize UI')).toBeVisible();
  });

  it('should not show the "Customize UI" link when no clientId is provided', () => {
    renderComponent(true);
    expect(screen.queryByText('Customize UI')).not.toBeInTheDocument();
  });

  it('should show the "Manage Users" link when the environment is not prod', () => {
    renderComponent(true);
    expect(screen.getByText('Manage Users')).toBeVisible();
  });

  it('should not show the "Manage Users" link when the environment is prod', () => {
    // eslint-disable-next-line functional/immutable-data
    ENV.CURRENT_ENV = 'prod';
    renderComponent(true);
    expect(screen.queryByText('Manage Users')).not.toBeInTheDocument();
  });

  it('should call handleDrawerClose when the close button is clicked', () => {
    renderComponent(true);
    const closeButton = screen.getByTestId('close-drawer-button');
    fireEvent.click(closeButton);
    expect(handleDrawerClose).toHaveBeenCalledTimes(1);
  });
});
