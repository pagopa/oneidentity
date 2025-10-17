import { render, screen, fireEvent } from '@testing-library/react';
import FieldWithInfo from './FieldWithInfo';
import { TextField, Select, MenuItem } from '@mui/material';

describe('FieldWithInfo', () => {
  const tooltipText = 'This is a tooltip';

  it('should render with a TextField as a child and inputAdornment as true', () => {
    render(
      <FieldWithInfo tooltipText={tooltipText} inputAdornment>
        <TextField />
      </FieldWithInfo>
    );
    expect(screen.getByRole('textbox')).toBeInTheDocument();
    expect(screen.getByRole('button')).toBeInTheDocument();
  });

  it('should render with a Select as a child and inputAdornment as true', () => {
    render(
      <FieldWithInfo tooltipText={tooltipText} inputAdornment>
        <Select value="">
          <MenuItem value="1">Option 1</MenuItem>
        </Select>
      </FieldWithInfo>
    );
    expect(screen.getByRole('combobox')).toBeInTheDocument();
    expect(screen.getByRole('button')).toBeInTheDocument();
  });

  it('should render with a TextField as a child and inputAdornment as false', () => {
    render(
      <FieldWithInfo tooltipText={tooltipText}>
        <TextField />
      </FieldWithInfo>
    );
    expect(screen.getByRole('textbox')).toBeInTheDocument();
    expect(screen.getByRole('button')).toBeInTheDocument();
  });

  it('should render with a simple div as a child', () => {
    render(
      <FieldWithInfo tooltipText={tooltipText}>
        <div>Hello</div>
      </FieldWithInfo>
    );
    expect(screen.getByText('Hello')).toBeInTheDocument();
    expect(screen.getByRole('button')).toBeInTheDocument();
  });

  it('should show the tooltip on hover', async () => {
    render(
      <FieldWithInfo tooltipText={tooltipText}>
        <TextField />
      </FieldWithInfo>
    );
    const infoButton = screen.getByRole('button');
    fireEvent.mouseOver(infoButton);
    expect(await screen.findByText(tooltipText)).toBeInTheDocument();
  });
});
