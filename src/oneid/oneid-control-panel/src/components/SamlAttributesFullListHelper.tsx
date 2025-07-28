import { FormHelperText } from '@mui/material';
import Link from '@mui/material/Link';

const SamlAttributesHelperLink = () => (
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
);

export default SamlAttributesHelperLink;
