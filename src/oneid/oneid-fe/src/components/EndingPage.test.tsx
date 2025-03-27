import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';

import EndingPage from './EndingPage';
describe('EndingPage Component', () => {
  it('should render the title, description, and icon', () => {
    render(
      <EndingPage
        icon={<div data-testid="mock-icon" />}
        title="Test Title"
        description="Test Description"
        variantTitle="h4"
        variantDescription="body1"
      />
    );

    // Check if the title is rendered
    expect(screen.getByText('Test Title')).toBeInTheDocument();
    // Check if the description is rendered
    expect(screen.getByText('Test Description')).toBeInTheDocument();
    // Check if the icon is rendered
    expect(screen.getByTestId('mock-icon')).toBeInTheDocument();
  });

  it('should render the first button and trigger its onClick', () => {
    const onButtonClick = vi.fn();
    render(
      <EndingPage
        labelButton="First Button"
        onClickButton={onButtonClick}
        title={undefined}
        description={undefined}
      />
    );

    // Check if the first button is rendered
    const button = screen.getByText('First Button');
    expect(button).toBeInTheDocument();

    // Simulate a click on the first button
    fireEvent.click(button);
    expect(onButtonClick).toHaveBeenCalled();
  });

  it('should conditionally render the paragraph if passed', () => {
    render(
      <EndingPage
        description="Test Description"
        paragraph="Test Paragraph"
        title={undefined}
      />
    );

    // Check if the paragraph is rendered
    expect(screen.getByText('Test Paragraph')).toBeInTheDocument();
  });
});
