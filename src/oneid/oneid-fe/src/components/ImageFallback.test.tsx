import { render, screen, waitFor } from '@testing-library/react';
import { FallbackImgProps, ImageWithFallback } from './ImageFallback';

describe('ImageWithFallback', () => {
  const src = 'primary-image.jpg';
  const placeholder = 'fallback-image.jpg';

  const setup = (props?: Partial<FallbackImgProps>) => {
    const defaultProps: FallbackImgProps = {
      src,
      placeholder,
      alt: 'Test Image',
      ...props,
    };
    render(<ImageWithFallback {...defaultProps} />);
  };

  it('renders with the primary image source', () => {
    setup();
    const image = screen.getByRole('img') as HTMLImageElement;
    expect(image.src).toContain(src);
  });

  it('displays the placeholder image when the primary image fails to load', async () => {
    setup();
    const image = screen.getByRole('img') as HTMLImageElement;

    // Simulate image error event
    image.dispatchEvent(new Event('error'));

    // Wait for the src to update to the placeholder
    await waitFor(() => {
      expect(image.src).toContain(placeholder);
    });
  });

  it('does not enter an error loop if the placeholder image fails', async () => {
    setup();
    const image = screen.getByRole('img') as HTMLImageElement;

    // Trigger error on the primary image
    image.dispatchEvent(new Event('error'));
    await waitFor(() => {
      expect(image.src).toContain(placeholder);
    });

    // Trigger error again on the placeholder image
    image.dispatchEvent(new Event('error'));
    await waitFor(() => {
      expect(image.src).toContain(placeholder);
    }); // Ensure it remains the same
  });

  it('applies additional props to the img element', () => {
    setup({ alt: 'Custom Alt Text', width: 200, height: 150 });
    const image = screen.getByRole('img') as HTMLImageElement;

    expect(image.alt).toBe('Custom Alt Text');
    expect(image.width).toBe(200);
    expect(image.height).toBe(150);
  });
});
