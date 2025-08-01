/* eslint-disable react/prop-types */
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { vi, describe, it, beforeEach, expect } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { GetUserList } from './GetUserList';
import { PropsWithChildren } from 'react';

vi.mock('react-oidc-context', () => ({
  useAuth: () => ({
    user: {
      id_token: 'fake-token',
      access_token: 'fake-token',
      profile: { email: 'test@example.com', sub: '123' },
    },
    isAuthenticated: true,
  }),
}));

const mockUseClient = vi.fn();
vi.mock('../../../hooks/useClient', () => ({
  useClient: () => mockUseClient(),
}));

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });

  const Wrapper: React.FC<PropsWithChildren> = ({ children }) => (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>{children}</BrowserRouter>
    </QueryClientProvider>
  );

  return Wrapper;
};

describe('GetUserList', () => {
  const mockUsers = [
    {
      username: 'test_uno',
      password: 'pass',
      samlAttributes: { name: 'name', ivaCode: '12345' },
    },
    {
      username: 'test_due',
      password: '1234',
      samlAttributes: { name: 'name', ivaCode: '12345' },
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders loading spinner when loading', () => {
    mockUseClient.mockReturnValue({
      getClientUsersList: {
        data: undefined,
        error: null,
        isLoading: true,
        isSuccess: false,
      },
      deleteClientUsersMutation: {
        mutate: vi.fn(),
        error: null,
        isSuccess: false,
        isPending: false,
      },
    });

    render(<GetUserList />, { wrapper: createWrapper() });

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  const mockMutate = vi.fn();
  it('renders user table when data is loaded', async () => {
    mockUseClient.mockReturnValue({
      getClientUsersList: {
        data: { users: mockUsers },
        error: null,
        isLoading: false,
        isSuccess: true,
      },
      deleteClientUsersMutation: {
        mutate: mockMutate,
        error: null,
        isSuccess: false,
        isPending: false,
      },
    });

    render(<GetUserList />, { wrapper: createWrapper() });

    expect(screen.getByText('User: test@example.com')).toBeInTheDocument();
    expect(await screen.findByText('test_uno')).toBeInTheDocument();
    expect(screen.getByText('test_due')).toBeInTheDocument();
  });

  it('opens modal and confirms delete via UserTable', async () => {
    render(<GetUserList />, { wrapper: createWrapper() });

    expect(await screen.findByText('test_uno')).toBeInTheDocument();
    expect(screen.getByText('test_due')).toBeInTheDocument();

    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
    expect(deleteButtons).toHaveLength(2);

    fireEvent.click(deleteButtons[0]);

    expect(
      await screen.findByText(/are you sure you want to delete this user/i)
    ).toBeInTheDocument();

    const confirmButton = screen.getByRole('button', { name: /^delete$/i });
    fireEvent.click(confirmButton);

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalledWith({ username: 'test_due' });
    });
  });

  it('handles delete user error', async () => {
    mockUseClient.mockReturnValueOnce({
      getClientUsersList: {
        data: { users: mockUsers },
        error: null,
        isLoading: false,
        isSuccess: true,
      },
      deleteClientUsersMutation: {
        mutate: mockMutate,
        error: new Error('Delete failed'),
        isSuccess: false,
        isPending: false,
      },
    });

    render(<GetUserList />, { wrapper: createWrapper() });

    const deleteButtons = await screen.findAllByRole('button', {
      name: /delete/i,
    });
    fireEvent.click(deleteButtons[0]);

    const confirmButton = await screen.findByRole('button', {
      name: /^delete$/i,
    });
    fireEvent.click(confirmButton);

    await waitFor(() => {
      expect(mockMutate).toHaveBeenCalled();
    });
  });
});
