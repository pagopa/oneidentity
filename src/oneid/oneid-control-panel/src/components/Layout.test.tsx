import { render } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import Layout from './Layout';
import { BrowserRouter } from 'react-router-dom';
import { ClientIdProvider } from '../context/ClientIdContext';
import { useAuth } from 'react-oidc-context';

vi.mock('react-oidc-context', () => ({
  useAuth: vi.fn(),
}));

describe('Layout - Auth Events', () => {
  const mockUnsubscribe = vi.fn();
  const mockRemoveUser = vi.fn();
  const mockSignoutRedirect = vi.fn();
  const mockAddAccessTokenExpired = vi.fn();
  const mockAddSilentRenewError = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();

    vi.mocked(useAuth).mockReturnValue({
      user: { profile: { email: 'test@example.com' } },
      isAuthenticated: true,
      events: {
        addAccessTokenExpired: mockAddAccessTokenExpired,
        addSilentRenewError: mockAddSilentRenewError,
      },
      removeUser: mockRemoveUser,
      signoutRedirect: mockSignoutRedirect,
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } as any);

    mockAddAccessTokenExpired.mockReturnValue(mockUnsubscribe);
  });

  it('should handle access token expiration by logging out and cleaning up', () => {
    let capturedCallback: (() => void) | undefined;

    // catch callback passed to addAccessTokenExpired
    mockAddAccessTokenExpired.mockImplementation((cb) => {
      capturedCallback = cb;
      return mockUnsubscribe;
    });

    const { unmount } = render(
      <BrowserRouter>
        <ClientIdProvider>
          <Layout>
            <div>Content</div>
          </Layout>
        </ClientIdProvider>
      </BrowserRouter>
    );

    // Simulate token expiration
    if (capturedCallback) {
      capturedCallback();
    }

    expect(mockRemoveUser).toHaveBeenCalled();
    expect(mockSignoutRedirect).toHaveBeenCalledWith(
      expect.objectContaining({
        extraQueryParams: expect.any(Object),
      })
    );

    unmount();
    expect(mockUnsubscribe).toHaveBeenCalledTimes(1);
  });

  it('should handle silent renew error by logging out and cleaning up', () => {
    let capturedCallback: ((e: unknown) => void) | undefined;
    const unsubscribeSilent = vi.fn();

    // catch callback passed to addSilentRenewError
    mockAddSilentRenewError.mockImplementation((cb) => {
      capturedCallback = cb;
      return unsubscribeSilent;
    });

    const { unmount } = render(
      <BrowserRouter>
        <ClientIdProvider>
          <Layout>
            <div>Content</div>
          </Layout>
        </ClientIdProvider>
      </BrowserRouter>
    );

    expect(mockAddSilentRenewError).toHaveBeenCalledTimes(1);

    // simulate silent renew error
    const err = new Error('renew failed');
    if (capturedCallback) capturedCallback(err);

    expect(mockRemoveUser).toHaveBeenCalled();
    expect(mockSignoutRedirect).toHaveBeenCalled();

    unmount();
    expect(unsubscribeSilent).toHaveBeenCalledTimes(1);
  });
});
