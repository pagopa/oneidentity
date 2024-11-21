import { render, screen } from '@testing-library/react';
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

  it('should render with the primary image source', () => {
    setup();
    const image = screen.getByRole('img') as HTMLImageElement;
    expect(image.src).toContain(src);
  });

  it('should display the placeholder image when the primary image fails to load', () => {
    setup();
    const image = screen.getByRole('img') as HTMLImageElement;

    // Simulate image error event directly
    image.dispatchEvent(new Event('error'));

    expect(image.src).toContain(placeholder);
  });

  it('should not trigger onError repeatedly if the placeholder image fails', async () => {
    const consoleSpy = vi
      .spyOn(console, 'error')
      .mockImplementation(() => null);
    setup();

    const image = screen.getByRole('img') as HTMLImageElement;

    // Simulate first error (sets placeholder as src)
    image.dispatchEvent(new Event('error'));
    expect(image.src).toContain(placeholder);

    // Trigger error again on the fallback image
    image.dispatchEvent(new Event('error'));

    // Check if the onError handler doesn't loop back to placeholder
    expect(image.src).toContain(placeholder);
    expect(consoleSpy).not.toHaveBeenCalled(); // No error loop

    consoleSpy.mockRestore();
  });

  it('should apply other provided props correctly', () => {
    setup({ alt: 'Custom Alt Text', width: 200 });
    const image = screen.getByRole('img') as HTMLImageElement;

    expect(image.alt).toBe('Custom Alt Text');
    expect(image.width).toBe(200);
  });
});
