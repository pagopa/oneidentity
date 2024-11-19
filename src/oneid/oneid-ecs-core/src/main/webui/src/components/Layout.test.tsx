import { render, screen } from '@testing-library/react';

import Layout from './Layout';

describe('Layout Component', () => {
  test('should render children elements', () => {
    render(
      <Layout>
        <div data-testid="mock-child">Child Content</div>
      </Layout>
    );

    // Check if the child content is rendered
    expect(screen.getByTestId('mock-child')).toBeInTheDocument();
    expect(screen.getByText('Child Content')).toBeInTheDocument();
  });
});
