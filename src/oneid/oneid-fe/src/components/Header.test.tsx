import { render, screen } from '@testing-library/react';
import Header from './Header';
import { useLoginData } from '../hooks/useLoginData';
import { Mock, vi } from 'vitest';
import { ENV } from '../utils/env';

vi.mock('../hooks/useLoginData');
vi.mock('../locale', async () => {
  const actual = await vi.importActual('../locale');
  return {
    ...actual,
    default: {
      ...(typeof actual.default === 'object' ? actual.default : {}),
      changeLanguage: vi.fn(),
    },
  };
});

vi.mock('../utils/env', () => ({
  ENV: {
    ASSISTANCE: {
      ENABLE: true,
      MOCK: true,
      EMAIL: 'fallback@example.com',
    },
    URL_FE: {
      LOGIN: '/login',
      LOGOUT: '/logout',
    },
    HEADER: {
      LINK: {
        PAGOPALINK: 'https://www.pagopa.it',
      },
    },
  },
}));

describe('Header Component', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });
  it('renders docUri correctly', () => {
    const mockClientQuery = {
      isFetched: true,
      data: {
        docUri: 'https://example.com/doc',
      },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);

    expect(
      screen.getByRole('button', { name: /Manuale operativo/ })
    ).toBeInTheDocument();

    vi.clearAllMocks();
  });

  it('hide docs button correctly when docUri not present', () => {
    const mockClientQuery = {
      isFetched: true,
      data: {},
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);
    expect(screen.queryByText(/Manuale operativo/)).toBeNull();

    vi.clearAllMocks();
  });

  it('hide docs button correctly when docUri empty', () => {
    const mockClientQuery = {
      isFetched: true,
      data: { docUri: '' },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);
    expect(screen.queryByText(/Manuale operativo/)).toBeNull();

    vi.clearAllMocks();
  });

  it('renders assistance button when enableAssistance is true and assistanceString is valid', () => {
    const mockClientQuery = {
      isFetched: true,
      data: {
        assistanceAddress: 'support@example.com',
      },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);

    expect(
      screen.getAllByRole('button', { name: /Assistenza/i }).length
    ).toBeGreaterThanOrEqual(1);
  });

  it('hides assistance button when enableAssistance is false', () => {
    ENV.ASSISTANCE.ENABLE = false;

    const mockClientQuery = {
      isFetched: true,
      data: {
        assistanceAddress: 'support@example.com',
      },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);

    expect(screen.queryByRole('button', { name: /Assistenza/i })).toBeNull();
  });

  it('uses ENV.ASSISTANCE.EMAIL when assistanceAddress is not provided', () => {
    ENV.ASSISTANCE.ENABLE = true;
    ENV.ASSISTANCE.EMAIL = 'fallback@example.com';

    const mockClientQuery = {
      isFetched: true,
      data: {},
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);

    expect(
      screen.getAllByRole('button', { name: /Assistenza/i }).length
    ).toBeGreaterThanOrEqual(1);
  });

  it('hides assistance button when assistanceString is invalid', () => {
    ENV.ASSISTANCE.ENABLE = true;
    ENV.ASSISTANCE.EMAIL = '';
    const mockClientQuery = {
      isFetched: true,
      data: {
        assistanceAddress: '',
      },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);

    expect(screen.queryByRole('button', { name: /Assistenza/i })).toBeNull();
  });

  it('hides assistance button when assistanceString is invalid', () => {
    ENV.ASSISTANCE.ENABLE = true;
    ENV.ASSISTANCE.EMAIL = '';
    const mockClientQuery = {
      isFetched: true,
      data: {
        assistanceAddress: 'support@example.com',
      },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);

    expect(
      screen.getAllByRole('button', { name: /Assistenza/i }).length
    ).toBeGreaterThanOrEqual(1);
  });
});
