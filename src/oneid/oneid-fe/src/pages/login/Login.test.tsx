import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { Mock, vi } from 'vitest';
import Login, { LinkWrapper } from '../login';
import { useLoginData } from '../../hooks/useLoginData';
import { ENV } from '../../utils/env';
import { trackEvent } from '../../services/analyticsService';
import { ThemeProvider } from '@emotion/react';
import { createTheme } from '@mui/material';

vi.mock('../../hooks/useLoginData');
vi.mock('../../services/analyticsService', () => ({
  trackEvent: vi.fn(),
}));
vi.mock('@mui/material', async () => {
  const actual = await vi.importActual('@mui/material');
  return {
    ...actual,
    useTheme: () => ({
      spacing: (value: number) => value * 8,
      breakpoints: { down: () => '@media (max-width: 960px)' },
    }),
  };
});

describe('LinkWrapper', () => {
  const mockOnClick = vi.fn();

  const renderWithTheme = (component: React.ReactNode) => {
    const theme = createTheme();
    return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
  };

  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('renders children correctly', () => {
    renderWithTheme(<LinkWrapper onClick={mockOnClick}>Test Link</LinkWrapper>);

    expect(screen.getByText('Test Link')).toBeInTheDocument();
  });

  it('calls onClick handler when clicked', () => {
    renderWithTheme(<LinkWrapper onClick={mockOnClick}>Click Me</LinkWrapper>);
    const link = screen.getByText('Click Me');

    fireEvent.click(link);

    expect(mockOnClick).toHaveBeenCalledTimes(1);
  });

  it('applies correct styles', () => {
    renderWithTheme(
      <LinkWrapper onClick={mockOnClick}>Styled Link</LinkWrapper>
    );
    const link = screen.getByText('Styled Link');

    expect(link).toHaveStyle({
      cursor: 'pointer',
    });
  });
});

describe('<Login />', () => {
  const mockBannerQuery = {
    isSuccess: true,
    data: [{ enable: true, severity: 'warning', description: 'Test banner' }],
  };
  const mockClientQuery = {
    isFetched: true,
    data: {
      friendlyName: 'Test Client',
      logoUri: 'https://example.com/logo.png',
      localizedContentMap: {},
    },
  };
  const mockIdpQuery = {
    isLoading: false,
    data: [
      {
        entityID: 'idp1',
        name: 'IDP 1',
        imageUrl: 'https://example.com/idp1.png',
      },
    ],
  };

  beforeEach(() => {
    (useLoginData as Mock).mockReturnValue({
      bannerQuery: mockBannerQuery,
      clientQuery: mockClientQuery,
      idpQuery: mockIdpQuery,
    });
  });

  it('renders titles and descriptions', () => {
    render(<Login />);
    expect(screen.getByText('loginPage.title')).toBeInTheDocument();
    expect(screen.getByText('loginPage.description')).toBeInTheDocument();
  });

  it('displays the client logo', () => {
    render(<Login />);
    const logo = screen.getByAltText('Test Client');
    expect(logo).toBeInTheDocument();
    expect(logo).toHaveAttribute('src', 'https://example.com/logo.png');
  });

  it('shows a banner when bannerQuery is successful', () => {
    render(<Login />);
    expect(screen.getByText('Test banner')).toBeInTheDocument();
  });

  it('opens the SpidModal on SPID button click', () => {
    render(<Login />);
    const spidButton = screen.getByRole('button', { name: /SPID/i });
    fireEvent.click(spidButton);
    expect(
      screen.getByRole('dialog', { name: 'spidSelect.modalTitle' })
    ).toBeInTheDocument();
  });

  it('navigates to CIE login on CIE button click', async () => {
    render(<Login />);
    const cieButton = screen.getByRole('button', { name: /CIE/i });
    fireEvent.click(cieButton);

    await waitFor(() => {
      expect(trackEvent).toHaveBeenCalledWith(
        'LOGIN_IDP_SELECTED',
        {
          SPID_IDP_NAME: 'CIE',
          SPID_IDP_ID: ENV.SPID_CIE_ENTITY_ID,
          FORWARD_PARAMETERS: expect.any(String),
        },
        expect.any(Function)
      );
    });
  });
});
