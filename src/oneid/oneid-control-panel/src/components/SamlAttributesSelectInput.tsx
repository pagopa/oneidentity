import {
  FormControl,
  FormHelperText,
  InputLabel,
  Link,
  MenuItem,
  OutlinedInput,
  Select,
  SelectChangeEvent,
} from '@mui/material';
import { SamlAttribute } from '../types/api';

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
      <FormHelperText>
        Full list:{' '}
        <Link
          target="_blank"
          rel="noopener noreferrer"
          href="https://docs.italia.it/italia/spid/spid-regole-tecniche/it/stabile/attributi.html"
        >
          here
        </Link>
      </FormHelperText>
    </FormControl>
  );
};

export default SamlAttributesSelectInput;
