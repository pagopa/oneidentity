import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';

import EndingPage from './EndingPage';

describe('EndingPage Component', () => {
  it('should render the title, description, and icon', () => {
    render(
      <EndingPage
        minHeight="52vh"
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
        buttonLabel="First Button"
        onButtonClick={onButtonClick}
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

  it('should render the second button if haveTwoButtons is true and trigger its onClick', () => {
    const onSecondButtonClick = vi.fn();
    render(
      <EndingPage
        buttonLabel="First Button"
        secondButtonLabel="Second Button"
        onSecondButtonClick={onSecondButtonClick}
        haveTwoButtons={true}
        title={undefined}
        description={undefined}
      />
    );

    // Check if both buttons are rendered
    const firstButton = screen.getByText('First Button');
    const secondButton = screen.getByText('Second Button');
    expect(firstButton).toBeInTheDocument();
    expect(secondButton).toBeInTheDocument();

    // Simulate a click on the second button
    fireEvent.click(secondButton);
    expect(onSecondButtonClick).toHaveBeenCalled();
  });

  it('should conditionally render the paragraph if isParagraphPresent is true', () => {
    render(
      <EndingPage
        description="Test Description"
        paragraph="Test Paragraph"
        isParagraphPresent={true}
        title={undefined}
      />
    );

    // Check if the paragraph is rendered
    expect(screen.getByText('Test Paragraph')).toBeInTheDocument();
  });

  it('should not render the paragraph if isParagraphPresent is false', () => {
    render(
      <EndingPage
        description="Test Description"
        paragraph="Test Paragraph"
        isParagraphPresent={false}
        title={undefined}
      />
    );

    // Ensure the paragraph is not rendered
    expect(screen.queryByText('Test Paragraph')).toBeNull();
  });
});
