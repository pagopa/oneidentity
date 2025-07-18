/* eslint-disable sonarjs/no-duplicate-string */
import React from 'react';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { Customize } from './Customize';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';

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

// Mocking the createTheme to avoid warnings in test environment
vi.mock('@mui/material/styles', async () => {
  const actual = await vi.importActual('@mui/material/styles');
  return {
    ...actual,
    createTheme: vi.fn(() => ({
      palette: { primary: { main: '#0066cc' }, secondary: { main: '#00c1b3' } },
      ...(actual as { createTheme: () => object }).createTheme(),
    })),
  };
});
const mockClientData = {
  a11yUri: 'https://example.com/a11y',
  backButtonEnabled: true,
  localizedContentMap: {
    default: {
      it: {
        title: 'Default Title',
        desc: 'Default Description',
        docUri: 'https://example.com/doc',
        cookieUri: 'https://example.com/cookie',
        supportAddress: 'support@example.com',
      },
    },
    enterprise: {
      fr: {
        title: 'frDefault Title',
        desc: 'frDefault Description',
        docUri: 'https://example.com/fr/doc',
        cookieUri: 'https://example.com/fr/cookie',
        supportAddress: '',
      },
      en: {
        title: 'enDefault Title',
        desc: 'enDefault Description',
        docUri: 'https://example.com/en/doc',
        cookieUri: 'https://example.com/en/cookie',
        supportAddress: '',
      },
    },
  },
};

vi.mock('../../hooks/useClient', () => ({
  useClient: () => ({
    getAdditionalClientAttrs: {
      data: mockClientData,
      isSuccess: true,
      error: null,
    },
    createOrUpdateClientAttrsMutation: {
      mutate: vi.fn(),
      error: null,
      isPending: false,
      data: mockClientData,
    },
    setCognitoProfile: {},
  }),
}));

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

describe('Refactored ReservedAreaForm Component', () => {
  it('should render the main header and ClientSettings component correctly', async () => {
    render(<Customize />, { wrapper: createWrapper() });

    expect(screen.getByText(/Client Configuration/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Client ID/i)).toHaveValue('');
    expect(screen.getByLabelText(/Back Button Enabled/i)).toBeChecked();
  });

  it('should render the ThemeManager with the default theme selected', async () => {
    render(<Customize />, { wrapper: createWrapper() });

    expect(screen.getByLabelText(/Back Button Enabled/i)).toBeChecked();
    expect(screen.getByText(/Localized Content Themes/i)).toBeInTheDocument();
    expect(screen.getByTestId('theme-select')).toHaveTextContent('default');
  });

  it('should render the LocalizedContentEditor with correct initial content', () => {
    render(<Customize />, { wrapper: createWrapper() });

    // Check for language tabs
    expect(screen.getByRole('tab', { name: /Italiano/i })).toBeInTheDocument();
    // expect(screen.getByRole('tab', { name: /English/i })).toBeInTheDocument();

    // Check for content of the active tab (Italiano)
    expect(screen.getByRole('tab', { name: /Italiano/i })).toHaveAttribute(
      'aria-selected',
      'true'
    );
    expect(screen.getByLabelText('Title')).toHaveValue('Default Title');
    expect(screen.getByLabelText('Support Address')).toHaveValue(
      'support@example.com'
    );
    expect(screen.getByLabelText('Documentation URI')).toHaveValue(
      'https://example.com/doc'
    );
  });

  // --- Integration and User Flow Tests ---

  it('should switch theme and update the LocalizedContentEditor', async () => {
    const user = userEvent.setup();
    render(<Customize />, { wrapper: createWrapper() });

    // 1. Click the theme selector to open the dropdown
    const themeSelect = screen.getByTestId('theme-select');
    // const selectLabel = /active theme/i;
    // const themeSelect = screen.getByLabelText(selectLabel);
    // const themeSelect = await screen.findByLabelText(selectLabel);
    // expect(themeSelect).toBeInTheDocument();
    const button = within(themeSelect).getByRole('combobox', {
      hidden: true,
    });
    await user.click(button);

    // 2. Wait for the dropdown to render and select the "enterprise" theme
    const enterpriseOption = screen.getByRole('option', {
      name: /enterprise/i,
    });
    await user.click(enterpriseOption);

    // 3. Assert the theme selector has updated
    expect(screen.getByTestId('theme-select')).toHaveTextContent('enterprise');

    // 4. Assert the language tabs have updated for the new theme
    expect(
      screen.queryByRole('tab', { name: /Italiano/i })
    ).not.toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /Français/i })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /Français/i })).toHaveAttribute(
      'aria-selected',
      'true'
    );

    // 5. Assert the content fields have updated
    expect(screen.getByLabelText('Title')).toHaveValue('frDefault Title');
    expect(screen.getByLabelText('Cookie Policy URI')).toHaveValue(
      'https://example.com/fr/cookie'
    );
  });

  it('should allow adding a new language to the active theme', async () => {
    const user = userEvent.setup();
    render(<Customize />, { wrapper: createWrapper() });

    // 1. Click the "Add Language" button
    await user.click(screen.getByRole('button', { name: /Add Language/i }));

    // 2. Modal should open. Select a new language (Deutsch)
    const languageSelect = screen.getByTestId('language-selector');
    const button = within(languageSelect).getByRole('combobox', {
      hidden: true,
    });
    await user.click(button);
    await user.click(screen.getByRole('option', { name: /deutsch/i }));

    // 3. Click the "Add" button in the modal
    await user.click(screen.getByRole('button', { name: 'Add' }));

    // 4. Assert the new tab is now present and active
    const newTab = screen.getByRole('tab', { name: /Deutsch/i });
    expect(newTab).toBeInTheDocument();
    expect(newTab).toHaveAttribute('aria-selected', 'true');

    // 5. Assert the new input fields are empty
    expect(screen.getByLabelText('Title')).toHaveValue('');
    expect(screen.getByLabelText('Support Address')).toHaveValue('');
  });

  it('should allow adding a new theme', async () => {
    const user = userEvent.setup();
    render(<Customize />, { wrapper: createWrapper() });

    // 1. Click the "Add New Theme" button
    await user.click(screen.getByRole('button', { name: /Add New Theme/i }));

    // 2. Type a name in the modal's text field
    const themeNameInput = await screen.findByLabelText(/New Theme Key/i);
    await user.type(themeNameInput, 'marketing');

    // 3. Click the "Create" button
    await user.click(screen.getByRole('button', { name: /Create/i }));

    // 4. Assert the new theme is now active
    expect(screen.getByTestId('theme-select')).toHaveTextContent('marketing');

    // It should have one default language (Italiano)
    expect(screen.getByRole('tab', { name: /Italiano/i })).toBeInTheDocument();
  });

  it('should prevent deleting the "default" theme', async () => {
    const user = userEvent.setup();
    render(<Customize />, { wrapper: createWrapper() });

    // 1. Ensure the active theme is 'default'
    const themeSelector = screen.getByTestId('theme-select');
    if (!themeSelector.textContent?.includes('default')) {
      await user.click(themeSelector);
      await user.click(screen.getByRole('option', { name: /default/i }));
    }

    // 2. Assert the delete button is disabled
    const deleteButton = screen.getByRole('button', {
      name: /Delete Current Theme/i,
    });
    expect(deleteButton).toBeDisabled();
  });

  it('should allow deleting a non-default theme', async () => {
    const user = userEvent.setup();
    render(<Customize />, { wrapper: createWrapper() });

    // 1. Switch to the 'enterprise' theme
    const themeSelect = screen.getByTestId('theme-select');
    const button = within(themeSelect).getByRole('combobox', {
      hidden: true,
    });
    await user.click(button);

    // 2. Wait for the dropdown to render and select the "enterprise" theme
    const enterpriseOption = screen.getByRole('option', {
      name: /enterprise/i,
    });
    await user.click(enterpriseOption);

    // 2. Assert the delete button is now enabled
    const deleteButton = screen.getByRole('button', {
      name: /Delete Current Theme/i,
    });
    expect(deleteButton).not.toBeDisabled();

    // 3. Click the delete button
    await user.click(deleteButton);

    // 4. Confirm deletion in the dialog
    const confirmDialog = await screen.findByRole('dialog');
    expect(confirmDialog).toBeInTheDocument();
    const confirmDeleteButton = within(confirmDialog).getByRole('button', {
      name: /Delete/i,
    });
    await user.click(confirmDeleteButton);

    // 5. Assert the 'enterprise' theme is gone and 'default' is now active
    expect(screen.queryByText('enterprise')).not.toBeInTheDocument();
    expect(screen.getByTestId('theme-select')).toHaveTextContent('default');
  });
});
