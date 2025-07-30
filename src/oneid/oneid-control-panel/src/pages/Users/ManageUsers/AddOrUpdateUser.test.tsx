/* eslint-disable react/prop-types */
import { render, screen, waitFor } from '@testing-library/react';
import type * as ReactRouterDom from 'react-router-dom';
import { vi, describe, it, beforeEach, expect } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AddOrUpdateUser } from './AddOrUpdateUser';
import userEvent from '@testing-library/user-event';
import { PropsWithChildren } from 'react';

vi.mock('react-oidc-context', () => ({
  useAuth: () => ({
    user: { profile: { email: 'test@example.com' } },
    isAuthenticated: true,
  }),
}));

const mockCreate = vi.fn();
const mockUpdate = vi.fn();

vi.mock('../../../hooks/useClient', () => ({
  useClient: () => ({
    createClientUsersMutation: {
      mutate: mockCreate,
      error: null,
      isSuccess: false,
      isPending: false,
    },
    updateClientUsersMutation: {
      mutate: mockUpdate,
      error: null,
      isSuccess: false,
      isPending: false,
    },
  }),
}));

type SamlAttributesSelectInputProps = {
  children?: React.ReactNode;
  onChangeFunction: (event: { target: { value: Array<string> } }) => void;
};

vi.mock('../../../components/SamlAttributesSelectInput', () => ({
  default: function SamlAttributesSelectInputMock({
    children,
    onChangeFunction,
  }: SamlAttributesSelectInputProps) {
    return (
      <div>
        <button
          aria-haspopup="listbox"
          onClick={() => onChangeFunction({ target: { value: ['name'] } })}
        >
          saml
        </button>
        {children}
      </div>
    );
  },
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

vi.mock('react-router-dom', async () => {
  const actual =
    await vi.importActual<typeof ReactRouterDom>('react-router-dom');

  return {
    ...actual,
    useParams: () => ({}),
    useLocation: () => ({ state: null }),
    useNavigate: () => vi.fn(),
  };
});
describe('AddOrUpdateUser', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders email of logged user', () => {
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });
    expect(screen.getByText('User: test@example.com')).toBeInTheDocument();
  });

  it('calls createClientUsersMutation when form is valid and submitted', async () => {
    const user = userEvent.setup();

    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await user.type(screen.getByLabelText(/Username/i), 'newuser');
    await user.type(screen.getByLabelText(/Password/i), 'password123');

    const selectButton = screen.getByRole('button', { name: /saml/i });
    await user.click(selectButton);
    const attributeInput = await screen.findByLabelText(/Value for name/i);

    await user.type(attributeInput, 'name');

    const submitBtn = screen.getByTestId('submit-button');
    expect(submitBtn).not.toBeDisabled();

    await user.click(submitBtn);

    await waitFor(() => {
      expect(mockCreate).toHaveBeenCalledWith({
        data: {
          username: 'newuser',
          password: 'password123',
          samlAttributes: { name: 'name' },
        },
      });
    });
  });

  it('calls updateClientUsersMutation when form is valid and submitted in edit mode', async () => {
    const rr = await import('react-router-dom');
    vi.spyOn(rr, 'useParams').mockReturnValue({ id: 'existing_user' });
    vi.spyOn(rr, 'useLocation').mockReturnValue({
      state: {
        userToEdit: {
          username: 'existing_user',
          password: 'password',
          samlAttributes: { name: 'OldValue' },
        },
      },
      key: '',
      pathname: '',
      search: '',
      hash: '',
    });

    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    const attributeInput = await screen.findByLabelText(/Value for name/i);
    await user.clear(attributeInput);
    await user.type(attributeInput, 'NewValue');

    const submitBtn = screen.getByTestId('submit-button');
    expect(submitBtn).not.toBeDisabled();

    await user.click(submitBtn);

    await waitFor(() => {
      expect(mockUpdate).toHaveBeenCalledWith({
        data: {
          username: 'existing_user',
          password: 'password',
          samlAttributes: { name: 'NewValue' },
        },
        username: 'existing_user',
      });
    });
  });
});
