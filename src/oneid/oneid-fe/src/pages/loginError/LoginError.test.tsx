/* eslint-disable functional/immutable-data */
import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { Mock } from 'vitest';

import { useLoginError } from '../../hooks/useLoginError';
import { LoginError } from './LoginError';
import { useLoginData } from '../../hooks/useLoginData';
import { ROUTE_LOGIN } from '../../utils/constants';

// Mocking the LoadingOverlay component
vi.mock('../../components/LoadingOverlay', () => ({
  LoadingOverlay: ({ loadingText }: { loadingText?: string }) => (
    <div>{loadingText || 'Loading...'}</div>
  ),
}));

vi.mock('../../hooks/useLoginData');
vi.mock('../../hooks/useLoginError');

const mockHandleErrorCode = vi.fn();

describe('LoginError Component', () => {
  const validCallbackURI = 'https://example.com/callback';
  const mockClientQuery = {
    isFetched: true,
    data: {
      clientID: 'test-client-id',
      friendlyName: 'Test Client',
      logoUri: 'https://example.com/logo.png',
      callbackURI: [validCallbackURI],
    },
  };

  beforeEach(() => {
    Object.defineProperty(window, 'location', {
      writable: true,
      value: {
        search: '?errorCode=19&redirect_uri=https://example.com',
        assign: vi.fn(),
      },
    });
    (useLoginError as Mock).mockReturnValue({
      handleErrorCode: mockHandleErrorCode,
    });
    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });
    mockHandleErrorCode.mockReturnValue({
      title: 'Test Title',
      description: 'Test Description',
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should display loading overlay when loading', () => {
    window.location.search = ''; // Reset location for this test
    render(
      <MemoryRouter>
        <LoginError />
      </MemoryRouter>
    );

    expect(screen.getByText(/loading/i)).toBeInTheDocument(); // Assuming you have a loading text
  });

  it('should display correct error page for errorCode 19', () => {
    render(
      <MemoryRouter>
        <LoginError />
      </MemoryRouter>
    );

    // Ensure loading overlay is not shown anymore
    expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();

    // Check if the title and description are rendered correctly
    expect(screen.getByText('Test Title')).toBeInTheDocument();
    expect(screen.getByText('Test Description')).toBeInTheDocument();
  });

  it('should display correct error page for unknown error code', () => {
    mockHandleErrorCode.mockReturnValue({
      title: 'Generic Error Title',
      description: 'Generic Error Description',
      haveRetryButton: false,
    });

    (useLoginError as Mock).mockReturnValue({
      handleErrorCode: mockHandleErrorCode,
    });

    // Set a different error code for this test
    window.location.search = '?errorCode=unknown';

    render(
      <MemoryRouter>
        <LoginError />
      </MemoryRouter>
    );

    expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();

    expect(screen.getByText('Generic Error Title')).toBeInTheDocument();
    expect(screen.getByText('Generic Error Description')).toBeInTheDocument();

    // Check if the retry button is not rendered
    expect(
      screen.queryByRole('button', { name: /retry/i })
    ).not.toBeInTheDocument();
  });

  it('should redirect to login if redirect_uri if do not match with one in /clients', () => {
    render(
      <MemoryRouter>
        <LoginError />
      </MemoryRouter>
    );

    const closeButton = screen.queryByRole('button', { name: /close/i });
    expect(closeButton).toBeInTheDocument();
    if (!closeButton) {
      throw new Error('Close button  not  found');
    }
    fireEvent.click(closeButton);
    expect(window.location.assign).toHaveBeenCalledWith(ROUTE_LOGIN);
  });

  it('should redirect to redirect_uri if present', () => {
    // Set correct redirect_uri for this test
    Object.defineProperty(window, 'location', {
      writable: true,
      value: {
        search: `?errorCode=19&redirect_uri=${validCallbackURI}`,
        assign: vi.fn(),
      },
    });
    render(
      <MemoryRouter>
        <LoginError />
      </MemoryRouter>
    );

    const closeButton = screen.queryByRole('button', { name: /close/i });
    expect(closeButton).toBeInTheDocument();
    if (!closeButton) {
      throw new Error('Close button not found');
    }
    fireEvent.click(closeButton);
    expect(window.location.assign).toHaveBeenCalledWith(
      `${validCallbackURI}?error=access_denied&error_description=19&state=null`
    );
  });

  it('should not redirect to redirect_uri if present and encoded but malformed', () => {
    // Set correct redirect_uri for this test
    Object.defineProperty(window, 'location', {
      writable: true,
      value: {
        search:
          '?errorCode=19&redirect_uri=https%3A%2F%example.com%3A8084%2Fcallback',
        assign: vi.fn(),
      },
    });
    render(
      <MemoryRouter>
        <LoginError />
      </MemoryRouter>
    );

    const closeButton = screen.queryByRole('button', { name: /close/i });
    expect(closeButton).toBeInTheDocument();
    if (!closeButton) {
      throw new Error('Close button not  found');
    }
    fireEvent.click(closeButton);
    expect(window.location.assign).toHaveBeenCalledWith(ROUTE_LOGIN);
  });

  it('should redirect to redirect_uri if present and encoded', () => {
    // Set correct redirect_uri for this test
    Object.defineProperty(window, 'location', {
      writable: true,
      value: {
        search:
          '?errorCode=19&redirect_uri=https%3A%2F%2Fexample.com%2Fcallback',
        assign: vi.fn(),
      },
    });
    render(
      <MemoryRouter>
        <LoginError />
      </MemoryRouter>
    );

    const closeButton = screen.queryByRole('button', { name: /close/i });
    expect(closeButton).toBeInTheDocument();
    if (!closeButton) {
      throw new Error('Close button not  found');
    }
    fireEvent.click(closeButton);
    expect(window.location.assign).toHaveBeenCalledWith(
      `${validCallbackURI}?error=access_denied&error_description=19&state=null`
    );
  });

  it('should redirect to login if redirect_uri is not present', () => {
    // Set different search params for this test
    Object.defineProperty(window, 'location', {
      writable: true,
      value: {
        search: '?errorCode=19',
        assign: vi.fn(),
      },
    });

    render(
      <MemoryRouter>
        <LoginError />
      </MemoryRouter>
    );
    const closeButton = screen.queryByRole('button', { name: /close/i });
    expect(closeButton).toBeInTheDocument();
    if (!closeButton) {
      throw new Error('Close button not found');
    }
    fireEvent.click(closeButton);

    expect(window.location.assign).toHaveBeenCalledWith(ROUTE_LOGIN);
  });
});
