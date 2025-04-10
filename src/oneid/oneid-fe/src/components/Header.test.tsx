/* eslint-disable functional/immutable-data */
import { fireEvent, render, screen } from '@testing-library/react';
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
const supportAddress = 'support@example.com';
const fallbackAddress = 'fallback@example.com';

vi.mock('../utils/env', () => ({
  ENV: {
    FALLBACK_ASSISTANCE: {
      ENABLE: true,
      MOCK: true,
      EMAIL: 'fallback@example.com',
    },
    HEADER: {
      LINK: {
        PAGOPALINK: 'https://www.pagopa.it',
      },
    },
    ANALYTICS: {
      ENABLE: false,
    },
  },
}));

describe('Header Component', () => {
  beforeEach(() => {
    window.open = vi.fn();
  });
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

  it('renders assistance button and use client supportAddress if present', () => {
    const mockClientQuery = {
      isFetched: true,
      data: {
        supportAddress,
      },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);
    const supportButton = screen.getByText('Assistenza', {
      selector: 'button',
    });

    expect(supportButton).toBeInTheDocument();
    if (!supportButton) {
      throw new Error('Support button is not found');
    }
    fireEvent.click(supportButton);
    expect(window.open).toHaveBeenCalledWith(
      `mailto:${supportAddress}`,
      '_blank'
    );
  });

  it('renders assistance button and use fallback ASSISTANCE.EMAIL if present and active', () => {
    ENV.FALLBACK_ASSISTANCE.ENABLE = true;
    ENV.FALLBACK_ASSISTANCE.EMAIL = fallbackAddress;
    const mockClientQuery = {
      isFetched: true,
      data: {
        supportAddress: '',
      },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);
    const supportButton = screen.getByText('Assistenza', {
      selector: 'button',
    });

    expect(supportButton).toBeInTheDocument();
    if (!supportButton) {
      throw new Error('Support button not found');
    }
    fireEvent.click(supportButton);
    expect(window.open).toHaveBeenCalledWith(
      `mailto:${ENV.FALLBACK_ASSISTANCE.EMAIL}`,
      '_blank'
    );
  });

  it('hide assistance button if client do not provide supportAddress and fallback are off', () => {
    ENV.FALLBACK_ASSISTANCE.ENABLE = false;
    ENV.FALLBACK_ASSISTANCE.EMAIL = fallbackAddress;
    const mockClientQuery = {
      isFetched: true,
      data: {
        supportAddress: '',
      },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);
    const supportButton = screen.queryByText('Assistenza', {
      selector: 'button',
    });

    expect(supportButton).not.toBeInTheDocument();
  });

  it('show assistance button when enableAssistance is false but client supportAddress is present', () => {
    ENV.FALLBACK_ASSISTANCE.ENABLE = false;

    const mockClientQuery = {
      isFetched: true,
      data: {
        supportAddress,
      },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);

    const supportButton = screen.getByText('Assistenza', {
      selector: 'button',
    });

    expect(supportButton).toBeInTheDocument();
    if (!supportButton) {
      throw new Error('Support button not found');
    }
    fireEvent.click(supportButton);
    expect(window.open).toHaveBeenCalledWith(
      `mailto:${supportAddress}`,
      '_blank'
    );
  });

  it('hide assistance button if client do not provide supportAddress and fallback are on but email is not provided', () => {
    ENV.FALLBACK_ASSISTANCE.ENABLE = true;
    ENV.FALLBACK_ASSISTANCE.EMAIL = '';
    const mockClientQuery = {
      isFetched: true,
      data: {
        supportAddress: '',
      },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);
    const supportButton = screen.queryByText('Assistenza', {
      selector: 'button',
    });

    expect(supportButton).not.toBeInTheDocument();
  });

  it('show assistance button when enableAssistance is false but client supportAddress is url and follow it', () => {
    ENV.FALLBACK_ASSISTANCE.ENABLE = false;

    const mockClientQuery = {
      isFetched: true,
      data: {
        supportAddress: 'https://example.com/support',
      },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header withSecondHeader />);

    const supportButton = screen.getByText('Assistenza', {
      selector: 'button',
    });

    expect(supportButton).toBeInTheDocument();
    if (!supportButton) {
      throw new Error('Support button is not found');
    }
    fireEvent.click(supportButton);
    expect(window.open).toHaveBeenCalledWith(
      'https://example.com/support',
      '_blank'
    );
  });
});
