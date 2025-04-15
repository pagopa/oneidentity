import { ButtonProps } from '@mui/base/Button/Button.types';
import {
  Button,
  Typography,
  SvgIconProps,
  Stack,
  ButtonOwnProps,
  TypographyOwnProps,
} from '@mui/material';
import { theme } from '@pagopa/mui-italia/dist/theme';
import { FunctionComponent, ReactElement, SVGProps } from 'react';

type EndingPageProps = {
  /** The ending page icon */
  icon?:
    | React.ReactElement<SvgIconProps>
    | FunctionComponent<SVGProps<SVGSVGElement>>
    | string;
  /** The ending page title */
  title: React.ReactNode;
  /** The ending page description */
  description: React.ReactNode;
  /** The ending page button label if any */
  variantTitle?: TypographyOwnProps['variant'];
  /** Set the variant of the description */
  variantDescription?: TypographyOwnProps['variant'];
  /** Set the text of paragraph */
  paragraph?: React.ReactNode;
  onClickButton?: ButtonProps['onClick'];
  labelButton?: React.ReactNode;
  variantButton?: ButtonOwnProps['variant'];
  /** The ending page second button if any */
  secondLabelButton?: React.ReactNode;
  onSecondButtonClick?: () => void;
  secondVariantButton?: ButtonOwnProps['variant'];
  haveTwoButtons?: boolean;
};

const EndingPage = ({
  description,
  icon,
  labelButton,
  onClickButton,
  paragraph,
  title,
  variantButton = 'contained',
  variantDescription,
  variantTitle,
  secondLabelButton,
  onSecondButtonClick,
  secondVariantButton = 'outlined',
  haveTwoButtons = false,
}: EndingPageProps) => (
  <Stack
    sx={{
      marginTop: '96px',
      justifyContent: { md: 'center', xs: 'flex-start' },
      alignItems: 'center',
      gap: 2,
    }}
  >
    {icon as ReactElement}
    <Stack textAlign="center" alignItems="center" gap={2} mt={2}>
      <Typography maxWidth={theme.spacing(56)} variant={variantTitle}>
        {title}
      </Typography>
      <Typography maxWidth={theme.spacing(62)} variant={variantDescription}>
        {description}
      </Typography>
    </Stack>

    <Stack direction="row" spacing={2} marginTop={2}>
      {labelButton && (
        <Button variant={variantButton} onClick={onClickButton}>
          {labelButton}
        </Button>
      )}
      {haveTwoButtons && (
        <Button variant={secondVariantButton} onClick={onSecondButtonClick}>
          {secondLabelButton}
        </Button>
      )}
    </Stack>

    {paragraph && (
      <Typography variant={variantDescription}>{paragraph}</Typography>
    )}
  </Stack>
);

export default EndingPage;
