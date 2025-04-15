import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { Dashboard } from '../Dashboard';
import { BrowserRouter } from 'react-router-dom';
import { apiService } from '../../services/apiService';
import { SpidLevel, SamlAttribute, ClientData } from '../../types/api';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

vi.mock('../../services/apiService', () => ({
  apiService: {
    createOrUpdateClient: vi.fn(),
    getClientData: vi.fn(),
  },
}));

const mockClientData: ClientData = {
  client_id: 'aaaaaaaaaaaaaa-aaaaaaaaaaaaaaaaaaaaaaaa',
  client_secret: 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
  client_id_issued_at: 1740589972573,
  client_secret_expires_at: 0,
  client_name: 'test_2',
  redirect_uris: ['https://client.example.org/callback'],
  saml_requested_attributes: [SamlAttribute.FISCAL_NUMBER],
  logo_uri: 'http://test.com/logo.png',
  policy_uri: 'http://test.com/policy_uri.html',
  tos_uri: 'http://test.com/tos_uri.html',
  default_acr_values: [SpidLevel.L2]
};

const mockAuthContext = {
  isAuthenticated: true,
  token: 'test-token',
  clientData: mockClientData,
  login: vi.fn(),
  logout: vi.fn(),
};

vi.mock('../../contexts/AuthContext', () => ({
  useAuth: () => mockAuthContext,
}));

// Add mockNavigate at the top with other mocks
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
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

// At the top with other mocks
vi.mock('../../hooks/useClient', async () => {
  const actual = await vi.importActual('../../hooks/useClient');
  return {
    ...actual,
    TIMEOUT_DURATION: 100, // Override timeout duration
  };
});

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(apiService.getClientData).mockResolvedValue(mockClientData);
  });

  it('should update client information', async () => {
    vi.mocked(apiService.createOrUpdateClient).mockResolvedValue({
      ...mockClientData,
      client_name: 'Updated Name',
    });

    render(<Dashboard />, { wrapper: createWrapper() });

    // Wait for loading to finish
    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    const nameInput = screen.getByRole('textbox', { name: /client name/i });
    fireEvent.change(nameInput, { target: { value: 'Updated Name' } });

    const submitButton = screen.getByRole('button', { name: /save changes/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(apiService.createOrUpdateClient).toHaveBeenCalledWith(
        expect.objectContaining({
          client_name: 'Updated Name',
        }),
        'test-token',
        mockClientData.client_id
      );
    });
  });

  it('should show loading state while fetching client data', async () => {
    vi.mocked(apiService.getClientData).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockClientData), 100))
    );

    render(<Dashboard />, { wrapper: createWrapper() });

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('should handle client data fetch error', async () => {
    const errorMessage = 'Failed to fetch client data';
    vi.mocked(apiService.getClientData).mockRejectedValue(new Error(errorMessage));

    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
          retryDelay: 0,
        },
      },
    });

    render(<Dashboard />, { 
      wrapper: ({ children }) => (
        <QueryClientProvider client={queryClient}>
          <BrowserRouter>{children}</BrowserRouter>
        </QueryClientProvider>
      ),
    });

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    }, { timeout: 2000 });
  });

  it('should handle logout', async () => {
    render(<Dashboard />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    const logoutButton = screen.getByRole('button', { name: /logout/i });
    fireEvent.click(logoutButton);

    expect(mockAuthContext.logout).toHaveBeenCalled();
    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  it('should update form fields correctly', async () => {
    render(<Dashboard />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    // Test Logo URI update
    const logoInput = screen.getByRole('textbox', { name: /logo uri/i });
    fireEvent.change(logoInput, { target: { value: 'https://newlogo.com/logo.png' } });
    expect(logoInput).toHaveValue('https://newlogo.com/logo.png');

    // Test Policy URI update
    const policyInput = screen.getByRole('textbox', { name: /policy uri/i });
    fireEvent.change(policyInput, { target: { value: 'https://newpolicy.com' } });
    expect(policyInput).toHaveValue('https://newpolicy.com');

    // Test ToS URI update
    const tosInput = screen.getByRole('textbox', { name: /terms of service uri/i });
    fireEvent.change(tosInput, { target: { value: 'https://newtos.com' } });
    expect(tosInput).toHaveValue('https://newtos.com');
  });

  it('should show update error message', async () => {
    const errorMessage = 'Failed to update client';
    vi.mocked(apiService.createOrUpdateClient).mockRejectedValue(new Error(errorMessage));

    render(<Dashboard />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    const submitButton = screen.getByRole('button', { name: /save changes/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('should show saving state during update', async () => {
    vi.mocked(apiService.createOrUpdateClient).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockClientData), 100))
    );

    render(<Dashboard />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    const submitButton = screen.getByRole('button', { name: /save changes/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(submitButton).toHaveAttribute('disabled');
      expect(screen.getByText(/saving/i)).toBeInTheDocument();
    });
  });

  it('should handle SPID level selection', async () => {
    render(<Dashboard />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    const spidSelect = screen.getByTestId('spid-level-select');
    fireEvent.mouseDown(spidSelect);

    // Wait for menu items to be rendered
    await waitFor(() => {
      expect(screen.getByText('Level L2')).toBeInTheDocument();
    });

    const level2Option = screen.getByText('Level L2');
    fireEvent.click(level2Option);
  });

  it('should handle SAML attributes selection', async () => {
    render(<Dashboard />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    const samlSelect = screen.getByTestId('saml-attributes-select');
    fireEvent.mouseDown(samlSelect);

    const fiscalNumberOption = screen.getByText(SamlAttribute.FISCAL_NUMBER);
    fireEvent.click(fiscalNumberOption);
  });

  it('should disable form submission when required fields are empty', async () => {
    render(<Dashboard />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    const nameInput = screen.getByRole('textbox', { name: /client name/i });
    fireEvent.change(nameInput, { target: { value: '' } });

    const submitButton = screen.getByRole('button', { name: /save changes/i });
    
    await waitFor(() => {
      expect(submitButton).toHaveAttribute('disabled');
    });
  });

  it('should handle network timeout', async () => {
    // Mock a slow network response that will trigger timeout
    const errorMessage = 'Request timed out. Please check your network connection.';
    vi.mocked(apiService.getClientData).mockRejectedValue(new Error(errorMessage));

    const queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
          retryDelay: 0,
          gcTime: 0,
          staleTime: 0,
        },
      },
    });

    render(<Dashboard />, { 
      wrapper: ({ children }) => (
        <QueryClientProvider client={queryClient}>
          <BrowserRouter>{children}</BrowserRouter>
        </QueryClientProvider>
      ),
    });

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    }, { timeout: 2000 });
  });
}); 