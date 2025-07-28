import {
  FormControl,
  FormHelperText,
  InputLabel,
  MenuItem,
  OutlinedInput,
  Select,
  SelectChangeEvent,
} from '@mui/material';
import { SamlAttribute } from '../types/api';
import SamlAttributesHelperLink from './SamlAttributesFullListHelper';

type SamlAttributesInputProps = {
  attributeSelectValues: Array<SamlAttribute> | undefined;
  onChangeFunction: (e: SelectChangeEvent<Array<SamlAttribute>>) => void;
  errorHelperText?: Array<string>;
  children?: React.ReactNode;
};

const SamlAttributesSelectInput = ({
  attributeSelectValues: inputValue,
  onChangeFunction,
  errorHelperText,
  children,
}: SamlAttributesInputProps) => {
  return (
    <FormControl fullWidth margin="normal" required error={!!errorHelperText}>
      <InputLabel id="saml-attributes-label">SAML Attributes</InputLabel>
      <Select
        labelId="saml-attributes-label"
        id="saml-attributes-select"
        multiple
        value={inputValue || []}
        onChange={onChangeFunction}
        input={<OutlinedInput label="SAML Attributes" />}
        data-testid="saml-attributes-select"
      >
        {Object.values(SamlAttribute).map((attr) => (
          <MenuItem key={attr} value={attr}>
            {attr}
          </MenuItem>
        ))}
      </Select>
      {children}
      <FormHelperText>{errorHelperText}</FormHelperText>
      <SamlAttributesHelperLink />
    </FormControl>
  );
};

export default SamlAttributesSelectInput;
