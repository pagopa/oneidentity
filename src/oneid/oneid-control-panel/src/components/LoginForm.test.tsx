/* eslint-disable sonarjs/no-duplicate-string */
/* eslint-disable functional/immutable-data */
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { LoginForm } from './LoginForm';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

export const mockAuthenticatedStatus = {
  isLoading: false,
  isAuthenticated: false,
  error: null as Error | null,
  client_id: null as string | null,
};

export const getMockAuthStatus = () => {
  return mockAuthenticatedStatus;
};

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  // eslint-disable-next-line react/display-name
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>{children}</BrowserRouter>
    </QueryClientProvider>
  );
};

describe('LoginForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    Object.defineProperty(window, 'location', { value: { assign: vi.fn() } });
    vi.mock('react-oidc-context', () => ({
      useAuth: () => {
        const { isLoading, isAuthenticated, error, client_id } =
          getMockAuthStatus();
        return {
          signinRedirect: vi.fn(),
          isLoading,
          isAuthenticated,
          error,
          removeUser: vi.fn(),
          settings: {},
          user: {
            id_token: 'fake-token',
            access_token: 'fake-token',
            profile: {
              email: 'test@example.com',
              'custom:client_id': client_id,
            },
          },
        };
      },
    }));
  });

  it('renders the login form', () => {
    render(<LoginForm />, { wrapper: createWrapper() });

    expect(screen.getByText(/OneIdentity Control Panel/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Login/i })).toBeInTheDocument();
  });

  it('redirects to the dashboard after successful login', async () => {
    mockAuthenticatedStatus.isAuthenticated = true;
    render(<LoginForm />, { wrapper: createWrapper() });

    const loginButton = screen.getByRole('button', { name: /Login/i });
    expect(loginButton).toBeInTheDocument();
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(global.window.location.assign).toHaveBeenCalledWith('/dashboard');
      // expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('redirects to the dashboard/client_id after successful login', async () => {
    mockAuthenticatedStatus.isAuthenticated = true;
    mockAuthenticatedStatus.client_id = 'mock-client-id';
    render(<LoginForm />, { wrapper: createWrapper() });

    const loginButton = screen.getByRole('button', { name: /Login/i });
    expect(loginButton).toBeInTheDocument();
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(global.window.location.assign).toHaveBeenCalledWith(
        '/dashboard/mock-client-id'
      );
    });
  });

  it('shows an error message if login fails', async () => {
    mockAuthenticatedStatus.isAuthenticated = false;
    mockAuthenticatedStatus.error = new Error('Login failed');

    render(<LoginForm />, { wrapper: createWrapper() });

    const loginButton = screen.getByRole('button', { name: /Login/i });
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(screen.getByText(/Login failed/i)).toBeInTheDocument();
      expect(global.window.location.assign).not.toHaveBeenCalledWith(
        '/dashboard'
      );
    });
  });

  it('handles unexpected errors during login', async () => {
    mockAuthenticatedStatus.isAuthenticated = false;
    mockAuthenticatedStatus.error = new Error('Unexpected error');
    render(<LoginForm />, { wrapper: createWrapper() });

    const loginButton = screen.getByRole('button', { name: /Login/i });
    fireEvent.click(loginButton);

    await waitFor(() => {
      expect(screen.getByText(/Unexpected error/i)).toBeInTheDocument();
      expect(global.window.location.assign).not.toHaveBeenCalledWith(
        '/dashboard'
      );
    });
  });
});
