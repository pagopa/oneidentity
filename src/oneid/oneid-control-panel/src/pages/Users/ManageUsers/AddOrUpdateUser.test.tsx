/* eslint-disable react/prop-types */
import { render, screen, waitFor, within } from '@testing-library/react';
import type * as ReactRouterDom from 'react-router-dom';
import { vi, describe, it, beforeEach, afterEach, expect, Mock } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AddOrUpdateUser } from './AddOrUpdateUser';
import userEvent from '@testing-library/user-event';
import { PropsWithChildren } from 'react';
import { NotificationProvider } from '../../../context/NotificationContext';
import { ROUTE_PATH } from '../../../utils/constants';
import { SamlAttribute } from '../../../types/api';

vi.mock('react-oidc-context', () => ({
  useAuth: () => ({
    user: { profile: { email: 'test@example.com', sub: 'mocked_user_id' } },
    isAuthenticated: true,
  }),
}));

const mockCreate = vi.fn();
const mockUpdate = vi.fn();
const mockNavigate = vi.fn();
const mockRefetch = vi.fn();
const mockUpdateClient = vi.fn();

let mockClientQuery: {
  data: { samlRequestedAttributes: Array<SamlAttribute> } | undefined;
  isSuccess: boolean;
  isFetching: boolean;
  error: Error | null;
  refetch: Mock;
};

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

const ariaInvalid = 'aria-invalid';

vi.mock('../../../hooks/useClient', () => ({
  useClient: () => ({
    createClientUsersMutation: mockCreateMutation,
    updateClientUsersMutation: mockUpdateMutation,
  }),
}));

vi.mock('../../../hooks/useRegister', () => ({
  useRegister: () => ({
    clientQuery: mockClientQuery,
    updateClientMutation: { mutate: mockUpdateClient },
  }),
}));

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });

  const Wrapper: React.FC<PropsWithChildren> = ({ children }) => (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <NotificationProvider>{children}</NotificationProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );

  return Wrapper;
};

const submitBtnId = 'submit-button';
const usernameInvalidError = 'username format is invalid';
const passwordInvalidError = 'password format is invalid';
const additionalAttributesButton = 'Add additional SAML attributes';
const removeFamilyNameButton = 'Remove familyName';

const fillCredentials = async (user: ReturnType<typeof userEvent.setup>) => {
  await user.type(screen.getByLabelText(/Username/i), 'newuser');
  await user.type(
    screen.getByLabelText(/Password/i, { selector: 'input' }),
    'password123'
  );
  await user.type(screen.getByLabelText(/Age/i), '30');
};

const openAdditionalAttributePicker = async (
  user: ReturnType<typeof userEvent.setup>
) => {
  await user.click(
    screen.getByRole('button', { name: additionalAttributesButton })
  );
  const dialog = screen.getByRole('dialog', {
    name: additionalAttributesButton,
  });
  await user.click(within(dialog).getByRole('button', { name: 'Continue' }));
  await user.click(
    within(dialog).getByRole('combobox', {
      name: 'Additional SAML attribute',
    })
  );
};

const addAdditionalAttribute = async (
  user: ReturnType<typeof userEvent.setup>,
  attribute: SamlAttribute
) => {
  await openAdditionalAttributePicker(user);
  await user.click(screen.getByRole('option', { name: attribute }));
  await user.click(screen.getByRole('button', { name: 'Add attribute' }));
  await waitFor(() => {
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });
};

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
    vi.setSystemTime(new Date('2026-11-13T12:00:00'));
    mockClientQuery = {
      data: { samlRequestedAttributes: [SamlAttribute.NAME] },
      isSuccess: true,
      isFetching: false,
      error: null,
      refetch: mockRefetch,
    };
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

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  it('calls createClientUsersMutation when form is valid and submitted', async () => {
    const user = userEvent.setup();

    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await fillCredentials(user);

    const attributeInput = screen.getByLabelText(/^name/i);

    await user.type(attributeInput, 'name');

    const submitBtn = screen.getByTestId(submitBtnId);
    expect(submitBtn).not.toBeDisabled();

    await user.click(submitBtn);

    await waitFor(() => {
      expect(mockCreate).toHaveBeenCalledWith({
        data: {
          username: 'newuser',
          password: 'password123',
          age: 30,
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
          age: 30,
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

    const attributeInput = await screen.findByLabelText(/^name/i);
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
          age: 30,
          samlAttributes: { name: 'NewValue' },
        },
        username: 'existing_user',
      });
    });
  });

  it('renders every client claim as a mandatory field without a global picker', () => {
    mockClientQuery = {
      ...mockClientQuery,
      data: {
        samlRequestedAttributes: [
          SamlAttribute.NAME,
          SamlAttribute.FISCAL_NUMBER,
        ],
      },
    };

    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    expect(screen.getByLabelText(/^name/i)).toBeRequired();
    expect(screen.getByLabelText(/^fiscalNumber/i)).toBeRequired();
    expect(screen.queryByRole('combobox')).not.toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: /^Remove / })
    ).not.toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: additionalAttributesButton })
    ).toBeInTheDocument();
  });

  it('requires all client claims even when only some have values', async () => {
    mockClientQuery = {
      ...mockClientQuery,
      data: {
        samlRequestedAttributes: [
          SamlAttribute.NAME,
          SamlAttribute.FISCAL_NUMBER,
        ],
      },
    };
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await fillCredentials(user);
    await user.type(screen.getByLabelText(/^name/i), 'Mario');
    await user.click(screen.getByTestId(submitBtnId));

    expect(mockCreate).not.toHaveBeenCalled();
    expect(screen.getByLabelText(/^fiscalNumber/i)).toHaveAttribute(
      ariaInvalid,
      'true'
    );

    await user.type(screen.getByLabelText(/^fiscalNumber/i), 'ABC123');
    await user.click(screen.getByTestId(submitBtnId));

    expect(mockCreate).toHaveBeenCalledWith({
      data: {
        username: 'newuser',
        password: 'password123',
        age: 30,
        samlAttributes: { name: 'Mario', fiscalNumber: 'ABC123' },
      },
    });
  });

  it('rejects whitespace-only required claim values', async () => {
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await fillCredentials(user);
    await user.type(screen.getByLabelText(/^name/i), '   ');
    await user.click(screen.getByTestId(submitBtnId));

    expect(mockCreate).not.toHaveBeenCalled();
    expect(screen.getByLabelText(/^name/i)).toHaveAttribute(
      ariaInvalid,
      'true'
    );
  });

  it('submits parsed values with surrounding whitespace removed', async () => {
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await fillCredentials(user);
    await user.type(screen.getByLabelText(/^name/i), '  Mario  ');
    await user.click(screen.getByTestId(submitBtnId));

    expect(mockCreate).toHaveBeenCalledWith({
      data: {
        username: 'newuser',
        password: 'password123',
        age: 30,
        samlAttributes: { name: 'Mario' },
      },
    });
  });

  it('shows only the warning before Continue and cancels without changing claims', async () => {
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await user.type(screen.getByLabelText(/^name/i), 'Mario');
    await user.click(
      screen.getByRole('button', { name: additionalAttributesButton })
    );

    const dialog = screen.getByRole('dialog', {
      name: additionalAttributesButton,
    });
    expect(dialog).toHaveTextContent(/Register tab/i);
    expect(dialog).toHaveTextContent(/propagat/i);
    expect(within(dialog).queryByRole('combobox')).not.toBeInTheDocument();
    expect(within(dialog).queryByRole('textbox')).not.toBeInTheDocument();
    expect(
      within(dialog).getByRole('button', { name: 'Continue' })
    ).toBeInTheDocument();

    await user.click(within(dialog).getByRole('button', { name: 'Cancel' }));

    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });
    expect(screen.getByLabelText(/^name/i)).toHaveValue('Mario');
    expect(screen.queryByLabelText(/^familyName/i)).not.toBeInTheDocument();
    expect(mockUpdateClient).not.toHaveBeenCalled();
  });

  it('cancels the optional picker without adding a field and warns again on reopening', async () => {
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await openAdditionalAttributePicker(user);
    await user.click(screen.getByRole('option', { name: 'familyName' }));
    await user.click(screen.getByRole('button', { name: 'Cancel' }));

    await waitFor(() => {
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });
    expect(screen.queryByLabelText(/^familyName/i)).not.toBeInTheDocument();
    await user.click(
      screen.getByRole('button', { name: additionalAttributesButton })
    );
    const dialog = screen.getByRole('dialog', {
      name: additionalAttributesButton,
    });
    expect(within(dialog).queryByRole('combobox')).not.toBeInTheDocument();
    expect(
      within(dialog).getByRole('button', { name: 'Continue' })
    ).toBeInTheDocument();
  });

  it('adds an optional claim only after confirmation and preserves the client configuration', async () => {
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await fillCredentials(user);
    await user.type(screen.getByLabelText(/^name/i), 'Mario');
    await addAdditionalAttribute(user, SamlAttribute.FAMILY_NAME);

    expect(screen.getByLabelText(/^familyName/i)).toHaveValue('');
    expect(
      screen.getByRole('heading', { name: 'Additional SAML attributes' })
    ).toBeInTheDocument();
    expect(screen.getByText(/Register tab/i)).toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: removeFamilyNameButton })
    ).toBeInTheDocument();

    await user.type(screen.getByLabelText(/^familyName/i), 'Rossi');
    await user.click(screen.getByTestId(submitBtnId));

    expect(mockCreate).toHaveBeenCalledWith({
      data: {
        username: 'newuser',
        password: 'password123',
        age: 30,
        samlAttributes: { name: 'Mario', familyName: 'Rossi' },
      },
    });
    expect(mockClientQuery.data?.samlRequestedAttributes).toEqual(['name']);
    expect(mockUpdateClient).not.toHaveBeenCalled();
  });

  it.each(['', '   '])(
    'rejects an added claim with a blank value (%j)',
    async (value) => {
      const user = userEvent.setup();
      render(<AddOrUpdateUser />, { wrapper: createWrapper() });

      await fillCredentials(user);
      await user.type(screen.getByLabelText(/^name/i), 'Mario');
      await addAdditionalAttribute(user, SamlAttribute.FAMILY_NAME);
      if (value) {
        await user.type(screen.getByLabelText(/^familyName/i), value);
      }
      await user.click(screen.getByTestId(submitBtnId));

      expect(mockCreate).not.toHaveBeenCalled();
      expect(screen.getByLabelText(/^familyName/i)).toHaveAttribute(
        ariaInvalid,
        'true'
      );
    }
  );

  it('removes an optional claim without removing required claims', async () => {
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await fillCredentials(user);
    await user.type(screen.getByLabelText(/^name/i), 'Mario');
    await addAdditionalAttribute(user, SamlAttribute.FAMILY_NAME);
    await user.click(
      screen.getByRole('button', { name: removeFamilyNameButton })
    );

    expect(screen.queryByLabelText(/^familyName/i)).not.toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: 'Remove name' })
    ).not.toBeInTheDocument();
    expect(screen.getByLabelText(/^name/i)).toHaveValue('Mario');
    await user.click(screen.getByTestId(submitBtnId));

    expect(mockCreate).toHaveBeenCalledWith({
      data: {
        username: 'newuser',
        password: 'password123',
        age: 30,
        samlAttributes: { name: 'Mario' },
      },
    });
  });

  it('excludes client-required and already selected claims from the optional picker', async () => {
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await addAdditionalAttribute(user, SamlAttribute.FAMILY_NAME);
    await openAdditionalAttributePicker(user);

    expect(
      screen.queryByRole('option', { name: 'name' })
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole('option', { name: 'familyName' })
    ).not.toBeInTheDocument();
    expect(
      screen.getByRole('option', { name: 'fiscalNumber' })
    ).toBeInTheDocument();
  });

  it('preserves existing optional edit claims and requires missing client claims', async () => {
    mockClientQuery = {
      ...mockClientQuery,
      data: {
        samlRequestedAttributes: [
          SamlAttribute.NAME,
          SamlAttribute.FISCAL_NUMBER,
        ],
      },
    };
    const rr = await import('react-router-dom');
    vi.spyOn(rr, 'useParams').mockReturnValue({ id: 'existing_user' });
    vi.spyOn(rr, 'useLocation').mockReturnValue({
      state: {
        userToEdit: {
          username: 'existing_user',
          password: 'password',
          age: 30,
          samlAttributes: { name: 'Mario', familyName: 'Rossi' },
        },
      },
      key: '',
      pathname: '',
      search: '',
      hash: '',
    });
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    expect(screen.getByLabelText(/^name/i)).toHaveValue('Mario');
    expect(screen.getByLabelText(/^familyName/i)).toHaveValue('Rossi');
    expect(screen.getByLabelText(/^fiscalNumber/i)).toHaveValue('');
    expect(screen.getByLabelText(/^fiscalNumber/i)).toBeRequired();
    expect(
      screen.getByRole('button', { name: removeFamilyNameButton })
    ).toBeInTheDocument();

    await user.click(screen.getByTestId(submitBtnId));
    expect(mockUpdate).not.toHaveBeenCalled();
    await user.type(screen.getByLabelText(/^fiscalNumber/i), 'ABC123');
    await user.click(screen.getByTestId(submitBtnId));

    expect(mockUpdate).toHaveBeenCalledWith({
      username: 'existing_user',
      data: {
        username: 'existing_user',
        password: 'password',
        age: 30,
        samlAttributes: {
          name: 'Mario',
          fiscalNumber: 'ABC123',
          familyName: 'Rossi',
        },
      },
    });

    mockUpdate.mockClear();
    await user.click(
      screen.getByRole('button', { name: removeFamilyNameButton })
    );
    await user.click(screen.getByTestId(submitBtnId));
    expect(mockUpdate).toHaveBeenCalledWith({
      username: 'existing_user',
      data: {
        username: 'existing_user',
        password: 'password',
        age: 30,
        samlAttributes: { name: 'Mario', fiscalNumber: 'ABC123' },
      },
    });
  });

  it.each([
    { isSuccess: false, isFetching: false },
    { isSuccess: false, isFetching: true },
    { isSuccess: true, isFetching: true },
  ])(
    'disables submission while client configuration is unavailable (%j)',
    (queryState) => {
      mockClientQuery = { ...mockClientQuery, ...queryState };
      render(<AddOrUpdateUser />, { wrapper: createWrapper() });

      expect(screen.getByTestId(submitBtnId)).toBeDisabled();
      expect(mockCreate).not.toHaveBeenCalled();
      expect(mockUpdate).not.toHaveBeenCalled();
    }
  );

  it('reports client configuration errors and retries without submitting', async () => {
    mockClientQuery = {
      ...mockClientQuery,
      data: undefined,
      isSuccess: false,
      error: new Error('Client unavailable'),
    };
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByTestId(submitBtnId)).toBeDisabled();
    await user.click(screen.getByRole('button', { name: /Retry/i }));

    expect(mockRefetch).toHaveBeenCalledTimes(1);
    expect(mockCreate).not.toHaveBeenCalled();
    expect(mockUpdate).not.toHaveBeenCalled();
  });

  it('disables submission when a successful query has no client data', () => {
    mockClientQuery = { ...mockClientQuery, data: undefined };
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    expect(screen.getByTestId(submitBtnId)).toBeDisabled();
  });

  it('renders claims arriving asynchronously without discarding entered credentials', async () => {
    mockClientQuery = {
      ...mockClientQuery,
      data: undefined,
      isSuccess: false,
      isFetching: true,
    };
    const user = userEvent.setup();
    const { rerender } = render(<AddOrUpdateUser />, {
      wrapper: createWrapper(),
    });
    await fillCredentials(user);
    expect(screen.getByTestId(submitBtnId)).toBeDisabled();

    mockClientQuery = {
      ...mockClientQuery,
      data: {
        samlRequestedAttributes: [
          SamlAttribute.NAME,
          SamlAttribute.FISCAL_NUMBER,
        ],
      },
      isSuccess: true,
      isFetching: false,
    };
    rerender(<AddOrUpdateUser />);

    expect(screen.getByLabelText(/Username/i)).toHaveValue('newuser');
    expect(screen.getByLabelText(/^name/i)).toBeRequired();
    expect(screen.getByLabelText(/^fiscalNumber/i)).toBeRequired();
    await user.type(screen.getByLabelText(/^name/i), 'Mario');
    await user.type(screen.getByLabelText(/^fiscalNumber/i), 'ABC123');
    await user.click(screen.getByTestId(submitBtnId));

    expect(mockCreate).toHaveBeenCalledWith({
      data: {
        username: 'newuser',
        password: 'password123',
        age: 30,
        samlAttributes: { name: 'Mario', fiscalNumber: 'ABC123' },
      },
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

  it('highlights username when backend validation returns a field message', async () => {
    mockCreateMutation = {
      mutate: mockCreate,
      error: new Error(usernameInvalidError),
      isSuccess: false,
      isPending: false,
    };

    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.getByText(usernameInvalidError)).toBeInTheDocument();
      expect(screen.getByLabelText(/Username/i)).toHaveAttribute(
        ariaInvalid,
        'true'
      );
    });
  });

  it('highlights password when backend validation returns a field message', async () => {
    mockCreateMutation = {
      mutate: mockCreate,
      error: new Error(passwordInvalidError),
      isSuccess: false,
      isPending: false,
    };

    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.getByText(passwordInvalidError)).toBeInTheDocument();
      expect(screen.getByLabelText(/Password/i)).toHaveAttribute(
        ariaInvalid,
        'true'
      );
    });
  });

  it('highlights username in edit mode when backend validation returns a field message', async () => {
    mockUpdateMutation = {
      mutate: mockUpdate,
      error: new Error(usernameInvalidError),
      isSuccess: false,
      isPending: false,
    };

    const rr = await import('react-router-dom');
    vi.spyOn(rr, 'useParams').mockReturnValue({ id: 'existing_user' });
    vi.spyOn(rr, 'useLocation').mockReturnValue({
      state: {
        userToEdit: {
          username: 'existing_user',
          password: 'password',
          age: 30,
          samlAttributes: { name: 'OldValue' },
        },
      },
      key: '',
      pathname: '',
      search: '',
      hash: '',
    });

    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.getByText(usernameInvalidError)).toBeInTheDocument();
      expect(screen.getByLabelText(/Username/i)).toHaveAttribute(
        ariaInvalid,
        'true'
      );
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

  it('shows validation errors when the form is submitted invalid', async () => {
    const user = userEvent.setup();

    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    const submitBtn = screen.getByTestId(submitBtnId);
    expect(submitBtn).not.toBeDisabled();

    await user.click(submitBtn);

    await waitFor(() => {
      expect(screen.getAllByText('Required')).toHaveLength(2);
      expect(
        screen.getByLabelText(/Username/i, { selector: 'input' })
      ).toHaveAttribute(ariaInvalid, 'true');
      expect(
        screen.getByLabelText(/Password/i, { selector: 'input' })
      ).toHaveAttribute(ariaInvalid, 'true');
      expect(screen.getByLabelText(/Age/i)).toHaveAttribute(
        ariaInvalid,
        'true'
      );
      expect(mockCreate).not.toHaveBeenCalled();
    });
  });

  it('renders the Age field as mandatory', () => {
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    expect(screen.getByLabelText(/Age/i)).toBeRequired();
    expect(screen.getByLabelText(/Age/i)).not.toHaveAttribute('readonly');
    expect(screen.getByLabelText(/Age/i)).toHaveAccessibleDescription(
      /internal IdP/i
    );
  });

  it('calculates read-only Age from a required birth date instead of the previous manual age', async () => {
    const user = userEvent.setup();
    const { rerender } = render(<AddOrUpdateUser />, {
      wrapper: createWrapper(),
    });
    await fillCredentials(user);
    await user.type(screen.getByLabelText(/^name/i), 'Mario');

    mockClientQuery = {
      ...mockClientQuery,
      data: {
        samlRequestedAttributes: [
          SamlAttribute.NAME,
          SamlAttribute.DATE_OF_BIRTH,
        ],
      },
    };
    rerender(<AddOrUpdateUser />);

    const age = screen.getByLabelText(/Age/i);
    const dateOfBirth = screen.getByLabelText(/^dateOfBirth/i);
    expect(age).toHaveAttribute('readonly');
    expect(age).not.toBeDisabled();
    expect(age).toHaveValue(null);
    expect(dateOfBirth).toHaveAttribute('type', 'date');
    expect(dateOfBirth).toBeRequired();
    await user.click(screen.getByTestId(submitBtnId));
    expect(mockCreate).not.toHaveBeenCalled();

    await user.type(dateOfBirth, '2000-11-14');
    expect(age).toHaveValue(25);
    await user.click(screen.getByTestId(submitBtnId));
    expect(mockCreate).toHaveBeenCalledWith({
      data: {
        username: 'newuser',
        password: 'password123',
        age: 25,
        samlAttributes: { name: 'Mario', dateOfBirth: '2000-11-14' },
      },
    });

    await user.clear(dateOfBirth);
    expect(age).toHaveValue(null);
    mockCreate.mockClear();
    await user.click(screen.getByTestId(submitBtnId));
    expect(mockCreate).not.toHaveBeenCalled();
  });

  it('locks Age for an additional birth date and restores editable calculated age on removal', async () => {
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });
    await fillCredentials(user);
    await user.type(screen.getByLabelText(/^name/i), 'Mario');
    await addAdditionalAttribute(user, SamlAttribute.DATE_OF_BIRTH);

    const age = screen.getByLabelText(/Age/i);
    expect(age).toHaveAttribute('readonly');
    expect(age).toHaveValue(null);
    await user.type(screen.getByLabelText(/^dateOfBirth/i), '2000-11-13');
    expect(age).toHaveValue(26);
    await user.click(
      screen.getByRole('button', { name: 'Remove dateOfBirth' })
    );

    expect(screen.queryByLabelText(/^dateOfBirth/i)).not.toBeInTheDocument();
    expect(age).not.toHaveAttribute('readonly');
    expect(age).toHaveValue(26);
    await user.click(screen.getByTestId(submitBtnId));
    expect(mockCreate).toHaveBeenCalledWith({
      data: {
        username: 'newuser',
        password: 'password123',
        age: 26,
        samlAttributes: { name: 'Mario' },
      },
    });
    await user.clear(age);
    await user.type(age, '40');
    await user.click(screen.getByTestId(submitBtnId));
    expect(mockCreate).toHaveBeenLastCalledWith({
      data: {
        username: 'newuser',
        password: 'password123',
        age: 40,
        samlAttributes: { name: 'Mario' },
      },
    });
  });

  it.each([
    ['2000-11-13', 26],
    ['2026-11-13', 0],
  ])(
    'replaces stale stored age when editing birth date %s',
    async (dateOfBirth, age) => {
      const rr = await import('react-router-dom');
      vi.spyOn(rr, 'useParams').mockReturnValue({ id: 'existing_user' });
      vi.spyOn(rr, 'useLocation').mockReturnValue({
        state: {
          userToEdit: {
            username: 'existing_user',
            password: 'password',
            age: 90,
            samlAttributes: { name: 'Mario', dateOfBirth },
          },
        },
        key: '',
        pathname: '',
        search: '',
        hash: '',
      });
      const user = userEvent.setup();
      render(<AddOrUpdateUser />, { wrapper: createWrapper() });

      expect(screen.getByLabelText(/Age/i)).toHaveAttribute('readonly');
      expect(screen.getByLabelText(/Age/i)).toHaveValue(age);
      await user.click(screen.getByTestId(submitBtnId));

      expect(mockUpdate).toHaveBeenCalledWith({
        username: 'existing_user',
        data: {
          username: 'existing_user',
          password: 'password',
          age,
          samlAttributes: { name: 'Mario', dateOfBirth },
        },
      });
    }
  );

  it.each(['2026-11-14', '2000-02-30', '13/11/2000'])(
    'blocks saving an invalid birth date (%s) instead of reusing stored age',
    async (dateOfBirth) => {
      const rr = await import('react-router-dom');
      vi.spyOn(rr, 'useParams').mockReturnValue({ id: 'existing_user' });
      vi.spyOn(rr, 'useLocation').mockReturnValue({
        state: {
          userToEdit: {
            username: 'existing_user',
            password: 'password',
            age: 30,
            samlAttributes: { name: 'Mario', dateOfBirth },
          },
        },
        key: '',
        pathname: '',
        search: '',
        hash: '',
      });
      const user = userEvent.setup();
      render(<AddOrUpdateUser />, { wrapper: createWrapper() });

      expect(screen.getByLabelText(/Age/i)).toHaveValue(null);
      expect(screen.getByLabelText(/Age/i)).toHaveAttribute('readonly');
      await user.click(screen.getByTestId(submitBtnId));

      expect(mockUpdate).not.toHaveBeenCalled();
      expect(screen.getByLabelText(/^dateOfBirth/i)).toHaveAttribute(
        ariaInvalid,
        'true'
      );
    }
  );

  it('places Age and every mandatory field before additional SAML attributes', async () => {
    mockClientQuery = {
      ...mockClientQuery,
      data: {
        samlRequestedAttributes: [
          SamlAttribute.NAME,
          SamlAttribute.FISCAL_NUMBER,
        ],
      },
    };
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await addAdditionalAttribute(user, SamlAttribute.ADDRESS);

    const password = screen.getByLabelText(/Password/i, {
      selector: 'input',
    });
    const age = screen.getByLabelText(/Age/i);
    const name = screen.getByLabelText(/^name/i);
    const additionalHeading = screen.getByRole('heading', {
      name: 'Additional SAML attributes',
    });
    const address = screen.getByLabelText(/^address/i);

    expect(password.compareDocumentPosition(age)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING
    );
    expect(age.compareDocumentPosition(name)).toBe(
      Node.DOCUMENT_POSITION_FOLLOWING
    );
    for (const field of [
      screen.getByLabelText(/Username/i),
      password,
      age,
      name,
      screen.getByLabelText(/^fiscalNumber/i),
    ]) {
      expect(field).toBeRequired();
      expect(field.compareDocumentPosition(additionalHeading)).toBe(
        Node.DOCUMENT_POSITION_FOLLOWING
      );
      expect(field.compareDocumentPosition(address)).toBe(
        Node.DOCUMENT_POSITION_FOLLOWING
      );
    }
  });

  it('blocks creation when the mandatory Age field is empty', async () => {
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    await user.type(screen.getByLabelText(/Username/i), 'newuser');
    await user.type(
      screen.getByLabelText(/Password/i, { selector: 'input' }),
      'password123'
    );

    const attributeInput = screen.getByLabelText(/^name/i);
    await user.type(attributeInput, 'name');

    const submitBtn = screen.getByTestId(submitBtnId);
    expect(submitBtn).not.toBeDisabled();
    await user.click(submitBtn);

    await waitFor(() => {
      expect(mockCreate).not.toHaveBeenCalled();
      expect(screen.getByLabelText(/Age/i)).toHaveAttribute(
        ariaInvalid,
        'true'
      );
    });
  });

  it.each([0, 1, 17, 999])(
    'includes a valid age in the mutation payload (%i)',
    async (age) => {
      const user = userEvent.setup();
      render(<AddOrUpdateUser />, { wrapper: createWrapper() });

      await user.type(screen.getByLabelText(/Username/i), 'newuser');
      await user.type(
        screen.getByLabelText(/Password/i, { selector: 'input' }),
        'password123'
      );

      const attributeInput = screen.getByLabelText(/^name/i);
      await user.type(attributeInput, 'name');

      const ageInput = screen.getByLabelText(/Age/i);
      await user.type(ageInput, String(age));

      const submitBtn = screen.getByTestId(submitBtnId);
      expect(submitBtn).not.toBeDisabled();
      await user.click(submitBtn);

      await waitFor(() => {
        expect(mockCreate).toHaveBeenCalledWith({
          data: expect.objectContaining({ age }),
        });
      });
    }
  );

  it('requires an age when editing a legacy user without one', async () => {
    const rr = await import('react-router-dom');
    vi.spyOn(rr, 'useParams').mockReturnValue({ id: 'existing_user' });
    vi.spyOn(rr, 'useLocation').mockReturnValue({
      state: {
        userToEdit: {
          username: 'existing_user',
          password: 'password',
          samlAttributes: { name: 'Mario' },
        },
      },
      key: '',
      pathname: '',
      search: '',
      hash: '',
    });
    const user = userEvent.setup();
    render(<AddOrUpdateUser />, { wrapper: createWrapper() });

    expect(screen.getByLabelText(/Age/i)).toHaveValue(null);
    await user.click(screen.getByTestId(submitBtnId));
    expect(mockUpdate).not.toHaveBeenCalled();
    expect(screen.getByLabelText(/Age/i)).toHaveAttribute(ariaInvalid, 'true');

    await user.type(screen.getByLabelText(/Age/i), '30');
    await user.click(screen.getByTestId(submitBtnId));

    expect(mockUpdate).toHaveBeenCalledWith({
      username: 'existing_user',
      data: {
        username: 'existing_user',
        password: 'password',
        age: 30,
        samlAttributes: { name: 'Mario' },
      },
    });
  });

  it.each(['-1', '1000', '17.5'])(
    'rejects an age outside the integer range 0–999 (%s)',
    async (age) => {
      const user = userEvent.setup();
      render(<AddOrUpdateUser />, { wrapper: createWrapper() });

      await user.type(screen.getByLabelText(/Username/i), 'newuser');
      await user.type(
        screen.getByLabelText(/Password/i, { selector: 'input' }),
        'password123'
      );

      const attributeInput = screen.getByLabelText(/^name/i);
      await user.type(attributeInput, 'name');

      const ageInput = screen.getByLabelText(/Age/i);
      await user.type(ageInput, age);

      const submitBtn = screen.getByTestId(submitBtnId);
      expect(submitBtn).not.toBeDisabled();

      await user.click(submitBtn);

      await waitFor(() => {
        expect(
          screen.getByLabelText(/Age/i, { selector: 'input' })
        ).toHaveAttribute(ariaInvalid, 'true');
        expect(mockCreate).not.toHaveBeenCalled();
      });
    }
  );
});
