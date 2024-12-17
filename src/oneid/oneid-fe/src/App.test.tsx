/* eslint-disable functional/immutable-data */
import { render } from '@testing-library/react';
import { vi } from 'vitest';

import App from './App';
import {
  ROUTE_LOGIN,
  ROUTE_LOGOUT,
  ROUTE_LOGIN_ERROR,
} from './utils/constants';
import { redirectToLogin } from './utils/utils';

// Mock imported functions
vi.mock('./utils/utils', () => ({
  redirectToLogin: vi.fn(),
}));
vi.mock('./services/analyticsService', () => ({
  trackEvent: vi.fn(),
}));

// Mock the components
vi.mock('./pages/login', () => ({
  default: () => <div>Mocked Login Component</div>,
}));
vi.mock('./pages/logout/Logout', () => ({
  default: () => <div>Mocked Logout Component</div>,
}));
vi.mock('./pages/loginError/LoginError', () => ({
  LoginError: () => <div>Mocked Login Error Component</div>,
}));

describe('App component with mocked components', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render the mocked Logout component on ROUTE_LOGOUT', () => {
    Object.defineProperty(window, 'location', {
      value: {
        pathname: ROUTE_LOGOUT,
      },
      writable: true,
    });

    const { getByText } = render(<App />);
    expect(getByText('Mocked Logout Component')).toBeInTheDocument();
  });

  it('should render the mocked Login component and track event on ROUTE_LOGIN', () => {
    Object.defineProperty(window, 'location', {
      value: {
        pathname: ROUTE_LOGIN,
      },
      writable: true,
    });

    const { getByText } = render(<App />);
    expect(getByText('Mocked Login Component')).toBeInTheDocument();
  });

  it('should render the mocked LoginError component on ROUTE_LOGIN_ERROR', () => {
    Object.defineProperty(window, 'location', {
      value: {
        pathname: ROUTE_LOGIN_ERROR,
      },
      writable: true,
    });

    const { getByText } = render(<App />);
    expect(getByText('Mocked Login Error Component')).toBeInTheDocument();
  });

  it('should redirect to login on unknown route', () => {
    Object.defineProperty(window, 'location', {
      value: {
        pathname: '/unknown-route',
      },
      writable: true,
    });

    render(<App />);
    expect(redirectToLogin).toHaveBeenCalled();
  });
});
