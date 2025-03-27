import { render, screen } from '@testing-library/react';
import Layout from './Layout';
import { useLoginData } from '../hooks/useLoginData';
import { Mock } from 'vitest';

vi.mock('../hooks/useLoginData');
describe('Layout Component', () => {
  const mockClientQuery = {
    isFetched: true,
    data: {
      friendlyName: 'Test Client',
      logoUri: 'https://example.com/logo.png',
    },
  };
  beforeEach(() => {
    (useLoginData as Mock).mockReturnValue({
      clientQuery: mockClientQuery,
    });
  });
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
