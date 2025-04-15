import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { LoginForm } from '../LoginForm';
import { BrowserRouter } from 'react-router-dom';
import { apiService } from '../../services/apiService';
import { ClientData } from '../../types/api';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// Mock apiService
vi.mock('../../services/apiService', () => ({
  apiService: {
    verifyToken: vi.fn(),
    getClientData: vi.fn(),
  },
}));

// Mock navigation
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock auth context
const mockLogin = vi.fn();
vi.mock('../../contexts/AuthContext', () => ({
  useAuth: () => ({
    login: mockLogin,
    isAuthenticated: false,
  }),
}));

// At the top with other mocks
vi.mock('../../hooks/useAuth', async () => {
  const actual = await vi.importActual('../../hooks/useAuth');
  return {
    ...actual,
    TIMEOUT_DURATION: 100, // Override timeout duration
  };
});

// Create a wrapper with QueryClientProvider
const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
      mutations: {
        retry: false,
      },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>{children}</BrowserRouter>
    </QueryClientProvider>
  );
};

describe('LoginForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Reset online status before each test
    Object.defineProperty(navigator, 'onLine', { value: true, writable: true });
  });

  it('should handle successful login for existing client', async () => {
    const mockLoginResponse = {
      valid: true,
      client_id: 'test-client-id',
    };

    const mockClientData: Partial<ClientData> = {
      client_id: 'test-client-id',
      client_name: 'Test Client',
      redirect_uris: ['https://test.com'],
      saml_requested_attributes: [],
      default_acr_values: [],
    };

    vi.mocked(apiService.verifyToken).mockResolvedValue(mockLoginResponse);
    vi.mocked(apiService.getClientData).mockResolvedValue(mockClientData as ClientData);

    render(<LoginForm />, { wrapper: createWrapper() });

    const tokenInput = screen.getByRole('textbox', { name: /api token/i });
    fireEvent.change(tokenInput, { target: { value: 'test-token' } });

    const submitButton = screen.getByRole('button', { name: /login/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(apiService.verifyToken).toHaveBeenCalledWith(
        'test-token',
        expect.any(AbortSignal)
      );
      expect(apiService.getClientData).toHaveBeenCalledWith(
        'test-client-id',
        'test-token',
        expect.any(AbortSignal)
      );
      expect(mockLogin).toHaveBeenCalledWith('test-token', mockClientData);
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('should handle error states', async () => {
    vi.mocked(apiService.verifyToken).mockRejectedValue(new Error('Invalid token'));

    render(<LoginForm />, { wrapper: createWrapper() });

    const tokenInput = screen.getByRole('textbox', { name: /api token/i });
    fireEvent.change(tokenInput, { target: { value: 'invalid-token' } });

    const submitButton = screen.getByRole('button', { name: /login/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/invalid token/i)).toBeInTheDocument();
    });
  });

  it('should show loading state during submission', async () => {
    vi.mocked(apiService.verifyToken).mockImplementation(
      () => new Promise((resolve) => setTimeout(resolve, 100))
    );

    render(<LoginForm />, { wrapper: createWrapper() });

    const tokenInput = screen.getByRole('textbox', { name: /api token/i });
    fireEvent.change(tokenInput, { target: { value: 'test-token' } });
    
    const submitButton = screen.getByRole('button', { name: /login/i });
    fireEvent.click(submitButton);

    // Wait for the loading state
    await waitFor(() => {
      expect(submitButton).toHaveAttribute('disabled');
      expect(screen.getByText(/logging in/i)).toBeInTheDocument();
    });
  });

  it('should handle network timeout during login', async () => {
    // Mock a slow network response that will trigger timeout
    vi.mocked(apiService.verifyToken).mockRejectedValueOnce(
      new Error('Login request timed out. Please check your network connection.')
    );

    render(<LoginForm />, { wrapper: createWrapper() });

    const tokenInput = screen.getByRole('textbox', { name: /api token/i });
    fireEvent.change(tokenInput, { target: { value: 'test-token' } });
    
    const submitButton = screen.getByRole('button', { name: /login/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/login request timed out/i)).toBeInTheDocument();
      expect(submitButton).not.toBeDisabled();
      expect(screen.queryByText(/logging in/i)).not.toBeInTheDocument();
    });
  });

  it('should handle immediate network failure', async () => {
    vi.mocked(apiService.verifyToken).mockRejectedValue(new Error('Network Error'));

    render(<LoginForm />, { wrapper: createWrapper() });

    const tokenInput = screen.getByRole('textbox', { name: /api token/i });
    fireEvent.change(tokenInput, { target: { value: 'test-token' } });
    
    const submitButton = screen.getByRole('button', { name: /login/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/network error/i)).toBeInTheDocument();
      expect(submitButton).not.toBeDisabled();
      expect(screen.queryByText(/logging in/i)).not.toBeInTheDocument();
    });
  });

  it('should handle offline state', () => {
    // Mock offline status
    Object.defineProperty(navigator, 'onLine', { value: false });
    
    render(<LoginForm />, { wrapper: createWrapper() });

    expect(screen.getByText(/offline/i)).toBeInTheDocument();
    expect(screen.getByRole('button')).toBeDisabled();
    expect(screen.getByText(/network error/i)).toBeInTheDocument();
  });

  it('should enable form when network comes back online', async () => {
    // Start offline
    Object.defineProperty(navigator, 'onLine', { value: false });
    
    render(<LoginForm />, { wrapper: createWrapper() });
    
    expect(screen.getByRole('button')).toBeDisabled();
    expect(screen.getByText('Offline')).toBeInTheDocument();

    // Simulate network coming back
    Object.defineProperty(navigator, 'onLine', { value: true });
    fireEvent(window, new Event('online'));

    await waitFor(() => {
      expect(screen.getByRole('button')).not.toBeDisabled();
      expect(screen.getByRole('button')).toHaveTextContent('Login');
      expect(screen.queryByText(/network error/i)).not.toBeInTheDocument();
    });
  });

  it('should prevent form submission when offline', async () => {
    Object.defineProperty(navigator, 'onLine', { value: false });
    
    render(<LoginForm />, { wrapper: createWrapper() });

    const tokenInput = screen.getByRole('textbox', { name: /api token/i });
    fireEvent.change(tokenInput, { target: { value: 'test-token' } });
    
    const submitButton = screen.getByRole('button');
    fireEvent.click(submitButton);

    expect(apiService.verifyToken).not.toHaveBeenCalled();
    expect(screen.getByText(/network error/i)).toBeInTheDocument();
  });
}); 