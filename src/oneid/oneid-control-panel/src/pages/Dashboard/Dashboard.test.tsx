/* eslint-disable sonarjs/no-duplicate-string */
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { Dashboard } from './Dashboard';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Layout from '../../components/Layout';

vi.mock('react-oidc-context', () => ({
  useAuth: () => ({
    user: {
      id_token: 'fake-token',
      access_token: 'fake-token',
      profile: { email: 'test@example.com' },
    },
    isAuthenticated: true,
    removeUser: vi.fn(),
    signoutRedirect: vi.fn(),
  }),
}));

const axiosMock = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  patch: vi.fn(),
}));

vi.mock('axios', async (importActual) => {
  const actual = await importActual<typeof import('axios')>();

  return {
    default: {
      ...actual.default,
      create: vi.fn(() => ({
        ...actual.default.create(),
        get: axiosMock.get,
        post: axiosMock.post,
        put: axiosMock.put,
        patch: axiosMock.patch,
      })),
    },
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

describe('Dashboard UI', () => {
  const clientID = '0000000000000000000000000000000000000000000';

  const mockClientData = {
    client_id: clientID,
    client_name: 'Test Client',
    logo_uri: 'https://example.com/logo.png',
    policy_uri: 'https://example.com/policy',
    tos_uri: 'https://example.com/tos',
  };

  beforeEach(() => {
    vi.clearAllMocks();
    axiosMock.post.mockResolvedValueOnce({
      data: mockClientData,
    });
    axiosMock.patch.mockResolvedValue({
      status: 204,
      data: null,
    });
  });

  it('renders the dashboard with form fields', async () => {
    render(<Dashboard />, { wrapper: createWrapper() });

    expect(screen.getByLabelText(/Client Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Logo URI/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Policy URI/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Terms of Service URI/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Redirect URIs/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/SPID Level/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/SAML Attributes/i)).toBeInTheDocument();
  });

  it('disables the submit button when required fields are empty', async () => {
    render(<Dashboard />, { wrapper: createWrapper() });

    const nameInput = screen.getByLabelText(/Client Name/i);
    fireEvent.change(nameInput, { target: { value: '' } });

    const submitButton = screen.getByRole('button', { name: /Create Client/i });
    expect(submitButton).toBeDisabled();
  });

  it('enables the submit button when required fields are filled', async () => {
    render(<Dashboard />, { wrapper: createWrapper() });

    // Fill the form with valid data
    const nameInput = screen.getByLabelText(/Client Name/i);
    fireEvent.change(nameInput, { target: { value: 'Test Client' } });

    const redirectUrisInput = screen.getByLabelText(/Redirect URIs/i);
    fireEvent.change(redirectUrisInput, {
      target: { value: 'https://example.com/callback' },
    });

    const spidSelect = screen.getByLabelText(/SPID Level/i);
    fireEvent.mouseDown(spidSelect);
    const level2Option = screen.getByText('Level L2');
    fireEvent.click(level2Option);

    const samlSelect = screen.getByLabelText(/SAML Attributes/i);
    fireEvent.mouseDown(samlSelect);
    const fiscalNumberOption = screen.getByText('fiscalNumber');
    fireEvent.click(fiscalNumberOption);

    // Ensure the submit button is enabled
    const submitButton = screen.getByTestId('submit-button');
    expect(submitButton).not.toBeDisabled();
  });

  it('shows a success notification after saving changes', async () => {
    render(<Dashboard />, { wrapper: createWrapper() });

    // Fill the form with valid data
    const nameInput = screen.getByLabelText(/Client Name/i);
    fireEvent.change(nameInput, { target: { value: 'Updated Client' } });

    const redirectUrisInput = screen.getByLabelText(/Redirect URIs/i);
    fireEvent.change(redirectUrisInput, {
      target: { value: 'https://example.com/callback' },
    });

    const spidSelect = screen.getByLabelText(/SPID Level/i);
    fireEvent.mouseDown(spidSelect);
    const level2Option = screen.getByText('Level L2');
    fireEvent.click(level2Option);

    const samlSelect = screen.getByLabelText(/SAML Attributes/i);
    fireEvent.mouseDown(samlSelect);
    const fiscalNumberOption = screen.getByText('fiscalNumber');
    fireEvent.click(fiscalNumberOption);

    // Ensure the submit button is enabled
    const submitButton = screen.getByTestId('submit-button');
    expect(submitButton).not.toBeDisabled();

    // Click the submit button
    fireEvent.click(submitButton);
    // modal dialog
    const confirmDialog = await screen.findByRole('dialog', {
      name: /confirm/i,
    });
    expect(confirmDialog).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /confirm/i }));
    // Wait for the success notification
    await waitFor(() => {
      expect(
        screen.getByText(/Client created successfully/i)
      ).toBeInTheDocument();
    });
  });

  it('shows an error notification when saving fails', async () => {
    render(<Dashboard />, { wrapper: createWrapper() });

    const nameInput = screen.getByLabelText(/Client Name/i);
    fireEvent.change(nameInput, { target: { value: 'Invalid data' } });

    const redirectUrisInput = screen.getByLabelText(/Redirect URIs/i);
    fireEvent.change(redirectUrisInput, {
      target: { value: 'invalid' },
    });

    const spidSelect = screen.getByLabelText(/SPID Level/i);
    fireEvent.mouseDown(spidSelect);
    const level2Option = screen.getByText('Level L2');
    fireEvent.click(level2Option);

    const samlSelect = screen.getByLabelText(/SAML Attributes/i);
    fireEvent.mouseDown(samlSelect);
    const fiscalNumberOption = screen.getByText('fiscalNumber');
    fireEvent.click(fiscalNumberOption);

    const submitButton = screen.getByTestId('submit-button');
    expect(submitButton).not.toBeDisabled();
    fireEvent.click(submitButton);

    // modal dialog
    const confirmDialog = await screen.findByRole('dialog', {
      name: /confirm/i,
    });
    expect(confirmDialog).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /confirm/i }));

    await waitFor(() => {
      expect(screen.getByText(/Error updating client/i)).toBeInTheDocument();
    });
  });

  it('handles logout correctly', async () => {
    render(
      <Layout>
        <Dashboard />
      </Layout>,
      { wrapper: createWrapper() }
    );

    const logoutButton = screen.getByTestId('logout-button');
    expect(logoutButton).toBeInTheDocument();
    fireEvent.click(logoutButton);

    await waitFor(() => {
      expect(window.location.pathname).toBe('/');
    });
  });
});
