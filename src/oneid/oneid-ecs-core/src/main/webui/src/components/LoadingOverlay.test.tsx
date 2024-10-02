import { render, screen } from '@testing-library/react';
import { LoadingOverlay } from './LoadingOverlay';
import '@testing-library/jest-dom';

// Mocking useTheme from Material-UI
vi.mock('@mui/material', async () => {
  const actual = await vi.importActual('@mui/material');
  return {
    ...actual,
    useTheme: () => ({
      palette: { primary: { main: '#1976d2' } },
    }),
  };
});

describe('LoadingOverlay Component', () => {
  test('should render the loading spinner and loading text', () => {
    const loadingText = 'Loading...';

    render(<LoadingOverlay loadingText={loadingText} />);

    // Check if loading spinner is rendered (since the spinner is a third-party component, it's enough to check its presence)
    expect(screen.getByRole('presentation')).toBeInTheDocument();

    // Check if the loading text is rendered
    expect(screen.getByText(loadingText)).toBeInTheDocument();
  });

  test('should not render loading text when none is provided', () => {
    render(<LoadingOverlay loadingText="" />);

    // Check that the spinner is present but no loading text
    expect(screen.getByRole('presentation')).toBeInTheDocument();
    expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
  });
});
