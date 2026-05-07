import { ReactNode } from 'react';
import { Divider, FormControlLabel, FormGroup, Switch } from '@mui/material';
import FieldWithInfo from './FieldWithInfo';

type ToggleSectionProps = {
  name: string;
  label: string;
  checked: boolean;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  tooltipText: ReactNode;
  withDivider?: boolean;
};

const ToggleSection = ({
  name,
  label,
  checked,
  onChange,
  tooltipText,
  withDivider = false,
}: ToggleSectionProps) => (
  <FormGroup sx={{ mt: 2, mb: 1 }}>
    {withDivider && <Divider sx={{ mb: 3 }} />}
    <FieldWithInfo tooltipText={tooltipText} placement="top">
      <FormControlLabel
        control={
          <Switch
            sx={{ mr: 2, ml: 1 }}
            name={name}
            checked={checked}
            onChange={onChange}
          />
        }
        label={label}
      />
    </FieldWithInfo>
  </FormGroup>
);

export default ToggleSection;
