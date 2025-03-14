import { render, screen, fireEvent } from '@testing-library/react';
import Footer from './Footer';
import { useLoginData } from '../../hooks/useLoginData';
import { Mock, vi } from 'vitest';
import i18n from '../../locale';

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
    render(<Footer loggedUser={false} />);

    expect(
      screen.getByLabelText('Vai al link: Informativa Privacy')
    ).toBeInTheDocument();
    expect(
      screen.getByLabelText(
        'Vai al link: Diritto alla protezione dei dati personali'
      )
    ).toBeInTheDocument();
    expect(
      screen.getByLabelText('Vai al link: Termini e Condizioni')
    ).toBeInTheDocument();
    expect(
      screen.getByLabelText('Vai al link: Accessibilità')
    ).toBeInTheDocument();
  });

  it('changes language when a new language is selected', async () => {
    render(<Footer loggedUser={false} />);

    const languageButton = screen.getByRole('button', { name: /lingua/i });
    fireEvent.click(languageButton);

    const englishOption = screen.getByRole('menuitem', { name: /english/i });
    fireEvent.click(englishOption);

    expect(i18n.changeLanguage).toHaveBeenCalledWith('en');
  });
});
