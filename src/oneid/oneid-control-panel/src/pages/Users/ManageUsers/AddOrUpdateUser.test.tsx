/* eslint-disable react/prop-types */
import { render, screen, waitFor } from '@testing-library/react';
import type * as ReactRouterDom from 'react-router-dom';
import { vi, describe, it, beforeEach, expect, Mock } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AddOrUpdateUser } from './AddOrUpdateUser';
import userEvent from '@testing-library/user-event';
import { PropsWithChildren } from 'react';
import { ROUTE_PATH } from '../../../utils/constants';

vi.mock('react-oidc-context', () => ({
  useAuth: () => ({
    user: { profile: { email: 'test@example.com', sub: 'mocked_user_id' } },
    isAuthenticated: true,
  }),
}));

const mockCreate = vi.fn();
const mockUpdate = vi.fn();
const mockNavigate = vi.fn();

let mockCreateMutation: {
  mutate: Mock;
  error: Error | null;
  isSuccess: boolean;
  isPending: boolean;
};

let mockUpdateMutation: {
  mutate: Mock;
  error: Error | null;
  isSuccess: boolean;
  isPending: boolean;
};

vi.mock('../../../hooks/useClient', () => ({
  useClient: () => ({
    createClientUsersMutation: mockCreateMutation,
    updateClientUsersMutation: mockUpdateMutation,
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

const submitBtnId = 'submit-button';

vi.mock('react-router-dom', async () => {
  const actual =
    await vi.importActual<typeof ReactRouterDom>('react-router-dom');

  return {
    ...actual,
    useParams: () => ({}),
    useLocation: () => ({ state: null }),
    useNavigate: () => mockNavigate,
  };
});
describe('AddOrUpdateUser', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockCreateMutation = {
      mutate: mockCreate,
      error: null,
      isSuccess: false,
      isPending: false,
    };
    mockUpdateMutation = {
      mutate: mockUpdate,
      error: null,
      isSuccess: false,
      isPending: false,
    };
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

    const submitBtn = screen.getByTestId(submitBtnId);
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

    const submitBtn = screen.getByTestId(submitBtnId);
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

  it('shows an error notification when create user fails', async () => {
    mockCreateMutation = {
      mutate: mockCreate,
      error: new Error('User already exists'),
      isSuccess: false,
      isPending: false,
    };
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });
    await waitFor(() => {
      expect(screen.getByText('User already exists')).toBeInTheDocument();
    });
  });

  it('shows an error notification when update user fails', async () => {
    mockUpdateMutation = {
      mutate: mockCreate,
      error: new Error('Error updating user'),
      isSuccess: false,
      isPending: false,
    };
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });
    await waitFor(() => {
      expect(screen.getByText('Error updating user')).toBeInTheDocument();
    });
  });

  it('navigates to user list on successful user creation', async () => {
    mockCreateMutation = {
      mutate: mockCreate,
      error: null,
      isSuccess: true,
      isPending: false,
    };
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith(ROUTE_PATH.USER_LIST, {
        state: {
          refresh: true,
          notify: {
            open: true,
            message: 'User Created!',
            severity: 'success',
          },
        },
      });
    });
  });

  it('navigates to user list on successful user update', async () => {
    mockUpdateMutation = {
      mutate: mockCreate,
      error: null,
      isSuccess: true,
      isPending: false,
    };
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith(ROUTE_PATH.USER_LIST, {
        state: {
          refresh: true,
          notify: {
            open: true,
            message: 'User updated!',
            severity: 'success',
          },
        },
      });
    });
  });

  it('toggles password visibility', async () => {
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    const passwordInput = screen.getByLabelText(/Password/i, {
      selector: 'input',
    });
    await user.type(passwordInput, 'password123');

    const visibilityButton = screen.getByRole('button', {
      name: /toggle password visibility/i,
    });

    expect(screen.getByTestId('VisibilityOnIcon')).toBeInTheDocument();

    await user.click(visibilityButton);

    await waitFor(() => {
      expect(screen.getByTestId('VisibilityOffIcon')).toBeInTheDocument();
    });

    await user.click(visibilityButton);

    await waitFor(() => {
      expect(screen.getByTestId('VisibilityOnIcon')).toBeInTheDocument();
    });
  });

  it('should not submit if form is invalid', async () => {
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    const submitBtn = screen.getByTestId(submitBtnId);
    await user.click(submitBtn);

    await waitFor(() => {
      expect(mockCreate).not.toHaveBeenCalled();
    });
  });
});
