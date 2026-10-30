import {
  render,
  screen,
  fireEvent,
  waitFor,
  within,
} from '@testing-library/react';
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
vi.mock('../../utils/env', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../utils/env')>();
  return {
    ENV: {
      ...actual.ENV,
      get CIE_TEST_ENV_ENABLED() {
        return false;
      },
    },
  };
});
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
  const cieModalTitle = 'cieSelect.modalTitle';
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
      backButtonEnabled: true,
      eidasIndex: null,
    },
  };
  const mockIdpQuery = {
    isLoading: false,
    data: {
      identityProviders: [
        {
          entityID: 'idp1',
          friendlyName: 'IDP 1',
          imageUrl: 'https://example.com/idp1.png',
        },
        {
          entityID: 'eidas-idp',
          friendlyName: 'eIDAS IDP',
          imageUrl: 'https://example.com/eidas.png',
        },
      ],
      richiediSpid: 'https://example.com/spid',
    },
  };

  beforeEach(() => {
    vi.spyOn(ENV, 'CIE_TEST_ENV_ENABLED', 'get').mockReturnValue(false);
    (useLoginData as Mock).mockReturnValue({
      bannerQuery: mockBannerQuery,
      clientQuery: mockClientQuery,
      idpQuery: mockIdpQuery,
    });
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.mocked(trackEvent).mockReset();
    window.history.replaceState({}, '', '/');
    vi.unstubAllGlobals();
  });

  it('not renders back button', () => {
    const mockClientQuery = {
      isFetched: true,
      data: {
        localizedContentMap: {},
        backButtonEnabled: false,
      },
    };
    (useLoginData as Mock).mockReturnValue({
      bannerQuery: mockBannerQuery,
      clientQuery: mockClientQuery,
      idpQuery: mockIdpQuery,
    });
    render(<Login />);
    expect(screen.queryByText('common.backButtonText')).toBeNull();
    vi.clearAllMocks();
  });

  it('not break with not clients attribute', () => {
    const mockClientQuery = {
      isFetched: true,
      data: {},
    };
    (useLoginData as Mock).mockReturnValue({
      bannerQuery: mockBannerQuery,
      clientQuery: mockClientQuery,
      idpQuery: mockIdpQuery,
    });
    render(<Login />);
    expect(screen.getByText('loginPage.title')).toBeInTheDocument();
    vi.clearAllMocks();
  });

  it('renders back button', () => {
    render(<Login />);
    expect(screen.getByText('common.backButtonText')).toBeInTheDocument();
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
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

    await waitFor(() => {
      expect(trackEvent).toHaveBeenCalledWith(
        'LOGIN_IDP_SELECTED',
        {
          SPID_IDP_NAME: 'CIE',
          SPID_IDP_ID: ENV.CIE_ENTITY_ID,
          FORWARD_PARAMETERS: expect.any(String),
        },
        expect.any(Function)
      );
    });
  });

  it.each([
    [
      'cieSelect.preproduction',
      'https://preproduzione.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO',
    ],
    [
      'cieSelect.testing',
      'https://collaudo.idserver.servizicie.interno.gov.it/idp/profile/SAML2/POST/SSO',
    ],
  ])('selects %s and preserves the OIDC parameters', (label, entityId) => {
    vi.spyOn(ENV, 'CIE_TEST_ENV_ENABLED', 'get').mockReturnValue(true);
    const oidcParams = new URLSearchParams({
      response_type: 'CODE',
      scope: 'openid',
      client_id: 'test-client',
      state: 'io-uat',
      nonce: 'test-nonce',
      redirect_uri: 'https://test-client.example/client/cb',
      idp: 'previous-idp',
      lang: 'en',
    });
    window.history.replaceState({}, '', `/login?${oidcParams}`);
    const assign = vi.fn();
    vi.stubGlobal('location', { ...window.location, assign });
    vi.mocked(trackEvent).mockImplementation((_event, _properties, callback) =>
      callback?.()
    );
    render(<Login />);

    fireEvent.click(screen.getByRole('button', { name: /CIE/i }));
    const dialog = screen.getByRole('dialog', {
      name: cieModalTitle,
    });
    expect(trackEvent).not.toHaveBeenCalled();
    expect(assign).not.toHaveBeenCalled();

    fireEvent.click(within(dialog).getByRole('button', { name: label }));

    oidcParams.set('idp', entityId);
    expect(trackEvent).toHaveBeenCalledWith(
      'LOGIN_IDP_SELECTED',
      {
        SPID_IDP_NAME: 'CIE',
        SPID_IDP_ID: entityId,
        FORWARD_PARAMETERS: oidcParams.toString(),
      },
      expect.any(Function)
    );
    expect(assign).toHaveBeenCalledTimes(1);
    expect(assign).toHaveBeenCalledWith(
      `${ENV.URL_API.AUTHORIZE}?${oidcParams}`
    );
  });

  it('cancels the CIE selection without starting authentication', async () => {
    vi.spyOn(ENV, 'CIE_TEST_ENV_ENABLED', 'get').mockReturnValue(true);
    render(<Login />);
    fireEvent.click(screen.getByRole('button', { name: /CIE/i }));
    const dialog = screen.getByRole('dialog', {
      name: cieModalTitle,
    });
    fireEvent.click(
      within(dialog).getByRole('button', { name: 'spidSelect.cancelButton' })
    );

    await waitFor(() =>
      expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    );
    expect(trackEvent).not.toHaveBeenCalled();
    fireEvent.click(screen.getByRole('button', { name: /CIE/i }));
    expect(
      screen.getByRole('dialog', { name: cieModalTitle })
    ).toBeInTheDocument();
  });

  it('does not render eIDAS login by default', () => {
    render(<Login />);
    expect(screen.queryByRole('button', { name: /eIDAS/i })).toBeNull();
  });

  it('navigates to eIDAS login on eIDAS button click', async () => {
    (useLoginData as Mock).mockReturnValue({
      bannerQuery: mockBannerQuery,
      clientQuery: {
        ...mockClientQuery,
        data: {
          ...mockClientQuery.data,
          eidasIndex: 99,
        },
      },
      idpQuery: mockIdpQuery,
    });

    render(<Login />);
    const eidasButton = screen.getByRole('button', { name: /eIDAS/i });
    fireEvent.click(eidasButton);

    await waitFor(() => {
      expect(trackEvent).toHaveBeenCalledWith(
        'LOGIN_IDP_SELECTED',
        {
          SPID_IDP_NAME: 'EIDAS',
          SPID_IDP_ID: ENV.EIDAS_ENTITY_ID,
          FORWARD_PARAMETERS: expect.any(String),
        },
        expect.any(Function)
      );
    });
  });
});
