/* eslint-disable functional/immutable-data */
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { Mock } from 'vitest';

import { useLoginError } from '../../hooks/useLoginError';
import { LoginError } from './LoginError';

// Mocking the LoadingOverlay component
vi.mock('../../components/LoadingOverlay', () => ({
  LoadingOverlay: ({ loadingText }: { loadingText?: string }) => (
    <div>{loadingText || 'Loading...'}</div>
  ),
}));

vi.mock('../../hooks/useLoginError');

const mockHandleErrorCode = vi.fn();

describe('LoginError Component', () => {
  beforeEach(() => {
    // Reset the mock function before each test
    mockHandleErrorCode.mockReset();
  });

  it('should display loading overlay when loading', () => {
    (useLoginError as Mock).mockReturnValue({
      handleErrorCode: mockHandleErrorCode,
    });

    render(
      <MemoryRouter>
        <LoginError />
      </MemoryRouter>
    );

    expect(screen.getByText(/loading/i)).toBeInTheDocument(); // Assuming you have a loading text
  });

  it('should display correct error page for errorCode 19', () => {
    mockHandleErrorCode.mockReturnValue({
      title: 'Test Title',
      description: 'Test Description',
      haveRetryButton: true,
    });

    window.location = { search: '?errorCode=19' } as Location;

    (useLoginError as Mock).mockReturnValue({
      handleErrorCode: mockHandleErrorCode,
    });

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

    // Check if the retry button is rendered
    expect(screen.getByRole('button', { name: /retry/i })).toBeInTheDocument();
  });

  it('should display correct error page for unknown error code', () => {
    mockHandleErrorCode.mockReturnValue({
      title: 'Generic Error Title',
      description: 'Generic Error Description',
      haveRetryButton: false,
    });

    window.location = { search: '?errorCode=19' } as Location;

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

  // Add more tests for different error codes as needed
});
