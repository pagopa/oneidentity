import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect } from 'vitest';
import ToggleSection from './ToggleSection';

describe('ToggleSection', () => {
  const defaultProps = {
    name: 'testSwitch',
    label: 'Test Label',
    checked: false,
    onChange: vi.fn(),
    tooltipText: 'Tooltip text',
  };

  it('renders the label and switch', () => {
    render(<ToggleSection {...defaultProps} />);
    expect(screen.getByLabelText(/Test Label/i)).toBeInTheDocument();
  });

  it('renders the switch as unchecked when checked=false', () => {
    render(<ToggleSection {...defaultProps} checked={false} />);
    expect(screen.getByLabelText(/Test Label/i)).not.toBeChecked();
  });

  it('renders the switch as checked when checked=true', () => {
    render(<ToggleSection {...defaultProps} checked={true} />);
    expect(screen.getByLabelText(/Test Label/i)).toBeChecked();
  });

  it('calls onChange when the switch is clicked', () => {
    const onChange = vi.fn();
    render(<ToggleSection {...defaultProps} onChange={onChange} />);
    fireEvent.click(screen.getByLabelText(/Test Label/i));
    expect(onChange).toHaveBeenCalledTimes(1);
  });

  it('does not render a divider by default', () => {
    const { container } = render(<ToggleSection {...defaultProps} />);
    expect(container.querySelector('hr')).not.toBeInTheDocument();
  });

  it('renders a divider when withDivider=true', () => {
    const { container } = render(
      <ToggleSection {...defaultProps} withDivider />
    );
    expect(container.querySelector('hr')).toBeInTheDocument();
  });

  it('renders the info icon for the tooltip', () => {
    render(<ToggleSection {...defaultProps} />);
    expect(screen.getByTestId('info-icon')).toBeInTheDocument();
  });

  it('shows tooltip text on hover', async () => {
    render(<ToggleSection {...defaultProps} tooltipText="My tooltip" />);
    fireEvent.mouseOver(screen.getByTestId('info-icon'));
    await waitFor(() => {
      expect(screen.getByText('My tooltip')).toBeInTheDocument();
    });
  });

  it('renders ReactNode as tooltip text', async () => {
    render(
      <ToggleSection
        {...defaultProps}
        tooltipText={
          <span>
            Rich <strong>tooltip</strong>
          </span>
        }
      />
    );
    fireEvent.mouseOver(screen.getByTestId('info-icon'));
    await waitFor(() => {
      expect(screen.getByText('tooltip')).toBeInTheDocument();
    });
  });
});
