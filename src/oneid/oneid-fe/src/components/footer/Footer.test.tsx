import { render, screen, fireEvent } from '@testing-library/react';
import Footer from './Footer';
import { useLoginData } from '../../hooks/useLoginData';
import { Mock, vi } from 'vitest';
import i18n from '../../locale';
import { ENV } from '../../utils/env';
import { getPreLoginFooterLinkDefinitions } from '../../utils/constants';

vi.mock('../../hooks/useLoginData');
vi.mock('../../locale', async () => {
  const actual = await vi.importActual('../../locale');
  return {
    ...actual,
    default: {
      ...(typeof actual.default === 'object' ? actual.default : {}),
      changeLanguage: vi.fn(),
    },
  };
});

describe('Footer Component', () => {
  const mockClientQuery = {
    isFetched: true,
    data: {
      policyUri: 'https://example.com/privacy',
      tosUri: 'https://example.com/terms',
    },
  };

  beforeEach(() => {
    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });
    vi.clearAllMocks();
  });

  it('renders links correctly', () => {
    render(<Footer />);

    const preLoginFooterLinkDefinitions = getPreLoginFooterLinkDefinitions({
      cookieHref: ENV.FOOTER.LINK.COOKIE,
      accessibilityHref: ENV.FOOTER.LINK.ACCESSIBILITY,
    });
    const expectedFooterLinks = [
      ...preLoginFooterLinkDefinitions.aboutUs,
      ...preLoginFooterLinkDefinitions.resources,
      ...preLoginFooterLinkDefinitions.followUsSocial,
      ...preLoginFooterLinkDefinitions.followUs,
    ];

    expectedFooterLinks.forEach(({ ariaLabel, href }) => {
      expect(screen.getByLabelText(ariaLabel)).toHaveAttribute('href', href);
    });
  });

  it('changes language when a new language is selected', async () => {
    render(<Footer />);

    const languageButton = screen.getByRole('button', { name: /lingua/i });
    fireEvent.click(languageButton);

    const englishOption = screen.getByRole('menuitem', { name: /english/i });
    fireEvent.click(englishOption);

    expect(i18n.changeLanguage).toHaveBeenCalledWith('en');
  });
});
