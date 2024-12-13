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
  /** The minHeight of the component, can be 52vh or 100vh */
  minHeight?: '52vh' | '100vh';
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
};

/** Selfcare's Ending Page */
const EndingPage = ({
  description,
  icon,
  labelButton,
  minHeight = '52vh',
  onClickButton,
  paragraph,
  title,
  variantButton = 'contained',
  variantDescription,
  variantTitle,
}: EndingPageProps) => (
  <Stack
    sx={{
      minHeight: { md: minHeight, xs: 'auto' },
      marginTop: { md: 0, xs: '25%' },
      justifyContent: { md: 'center', xs: 'flex-start' },
      alignItems: 'center',
      gap: 2,
    }}
  >
    {icon as ReactElement}
    <Stack textAlign="center" alignItems="center" gap={1}>
      <Typography maxWidth={theme.spacing(56)} variant={variantTitle}>
        {title}
      </Typography>
      <Typography maxWidth={theme.spacing(62)} variant={variantDescription}>
        {description}
      </Typography>
    </Stack>
    {labelButton && (
      <Button variant={variantButton} onClick={onClickButton}>
        {labelButton}
      </Button>
    )}

    {paragraph && (
      <Typography variant={variantDescription}>{paragraph}</Typography>
    )}
  </Stack>
);

export default EndingPage;
