import { Link } from '@mui/material';
import { tooltipLinkSx } from '../utils/styles';

type TooltipContentProps = {
  text: string;
  infoUrl: string;
};

const TooltipContentWithLink = ({ text, infoUrl }: TooltipContentProps) => (
  <span>
    {text}
    <br />
    More info can be found{' '}
    <Link
      href={infoUrl}
      target="_blank"
      rel="noopener noreferrer"
      sx={tooltipLinkSx}
    >
      here
    </Link>
  </span>
);

export default TooltipContentWithLink;
