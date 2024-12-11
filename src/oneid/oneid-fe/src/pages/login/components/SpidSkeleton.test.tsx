import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { SpidSkeleton } from './SpidSkeleton';

describe('SpidSkeleton', () => {
  it('renders the SpidSkeleton component with the correct structure', () => {
    render(<SpidSkeleton />);

    // Check that the component has the correct role and aria-label
    const skeletonContainer = screen.getByRole('status', { name: 'loading' });
    expect(skeletonContainer).toBeInTheDocument();

    // Check that there are two primary Stack containers
    const stackContainers =
      skeletonContainer.querySelectorAll('.MuiStack-root');
    expect(stackContainers).toHaveLength(2);

    // Verify the presence of six Skeleton components (3 in each stack)
    const skeletons = screen.getAllByRole('presentation');
    expect(skeletons).toHaveLength(6);

    // Check that each Skeleton component has the correct attributes
    skeletons.forEach((skeleton) => {
      expect(skeleton).toHaveAttribute('aria-busy', 'true');
    });
  });

  it('has the correct styles for each Skeleton component', () => {
    render(<SpidSkeleton />);
    const skeletons = screen.getAllByRole('presentation');

    skeletons.forEach((skeleton) => {
      expect(skeleton).toHaveStyle({
        borderRadius: '4px',
        height: '48px',
        width: '148px',
      });
    });
  });
});
