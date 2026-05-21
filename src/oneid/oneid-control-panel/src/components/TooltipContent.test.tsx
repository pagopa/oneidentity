import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import TooltipContentWithLink from './TooltipContent';

describe('TooltipContentWithLink', () => {
  const text = 'This is the description text.';
  const infoUrl = 'https://example.com/info';

  it('renders the text', () => {
    render(<TooltipContentWithLink text={text} infoUrl={infoUrl} />);
    // text is split by a <br> so we query the container directly
    expect(
      screen.getByText(
        (_, el) =>
          el?.tagName === 'SPAN' && (el.textContent ?? '').includes(text)
      )
    ).toBeInTheDocument();
  });

  it('renders the "More info can be found" label', () => {
    render(<TooltipContentWithLink text={text} infoUrl={infoUrl} />);
    expect(screen.getByText(/More info can be found/i)).toBeInTheDocument();
  });

  it('renders a "here" link with the correct href', () => {
    render(<TooltipContentWithLink text={text} infoUrl={infoUrl} />);
    const link = screen.getByRole('link', { name: /here/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute('href', infoUrl);
  });

  it('renders the link with target="_blank" and rel="noopener noreferrer"', () => {
    render(<TooltipContentWithLink text={text} infoUrl={infoUrl} />);
    const link = screen.getByRole('link', { name: /here/i });
    expect(link).toHaveAttribute('target', '_blank');
    expect(link).toHaveAttribute('rel', 'noopener noreferrer');
  });
});
