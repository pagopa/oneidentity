import { render, screen } from '@testing-library/react';
import Header from './Header';
import { useLoginData } from '../hooks/useLoginData';
import { Mock, vi } from 'vitest';

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

describe('Header Component', () => {
  it('renders docUri correctly', () => {
    const mockClientQuery = {
      isFetched: true,
      data: {
        policyUri: 'https://example.com/privacy',
        tosUri: 'https://example.com/terms',
        backButtonEnabled: true,
        cookieUri: 'https://example.com/cookie',
        docUri: 'https://example.com/doc',
        a11yUri: 'https://example.com/a11y',
        logoUri: 'https://example.com/logo.png',
        localizedContentMap: {
          it: {
            title: 'title',
            description: 'description',
          },
        },
      },
    };

    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });

    render(<Header loggedUser={false} withSecondHeader />);

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

    render(<Header loggedUser={false} withSecondHeader />);
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

    render(<Header loggedUser={false} withSecondHeader />);
    expect(screen.queryByText(/Manuale operativo/)).toBeNull();

    vi.clearAllMocks();
  });

  // it('changes language when a new language is selected', async () => {
  //   render(<Header loggedUser={false} withSecondHeader={false} />);

  //   const languageButton = screen.getByRole('button', { name: /lingua/i });
  //   fireEvent.click(languageButton);

  //   const englishOption = screen.getByRole('menuitem', { name: /english/i });
  //   fireEvent.click(englishOption);

  //   expect(i18n.changeLanguage).toHaveBeenCalledWith('en');
  // });
});
