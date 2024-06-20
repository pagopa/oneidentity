import { Button, Grid, Typography, Box, SvgIconProps } from '@mui/material';
import { FunctionComponent, ReactElement, SVGProps } from 'react';

type Props = {
  /** The minHeight of the component, can be 52vh for the pages and 100vh for the blocking page */
  minHeight?: '52vh' | '100vh';
  /** The ending page icon */
  icon?: React.ReactElement<SvgIconProps> | FunctionComponent<SVGProps<SVGSVGElement>> | string;
  /** The ending page title */
  title: React.ReactNode;
  /** The ending page description */
  description: React.ReactNode;
  /** The ending page button label if any */
  buttonLabel?: React.ReactNode;
  /** The ending page second button label if any */
  secondButtonLabel?: React.ReactNode;
  /** if defined performe this action on click of the first button */
  onButtonClick?: () => void;
  /** if defined performe this action on click of the second button */
  onSecondButtonClick?: () => void;
  /** Set the variant of the title */
  variantTitle?:
    | 'button'
    | 'caption'
    | 'h1'
    | 'h2'
    | 'h3'
    | 'h4'
    | 'h5'
    | 'h6'
    | 'inherit'
    | 'subtitle1'
    | 'subtitle2'
    | 'body1'
    | 'body2'
    | 'overline'
    | undefined;
  /** Set the variant of the description */
  variantDescription?:
    | 'button'
    | 'caption'
    | 'h1'
    | 'h2'
    | 'h3'
    | 'h4'
    | 'h5'
    | 'h6'
    | 'inherit'
    | 'subtitle1'
    | 'subtitle2'
    | 'body1'
    | 'body2'
    | 'overline'
    | undefined;
  /** Set the variant of the first button */
  variantFirstButton?: 'contained' | 'outlined' | 'text';
  /** Set the variant of the second button */
  variantSecondButton?: 'contained' | 'outlined' | 'text';
  /** Set the text of paragraph */
  paragraph?: React.ReactNode;
  /** Show the paragraph */
  isParagraphPresent?: boolean;
  /** Show the second button and the "secondButtonLabel" as text of this one */
  haveTwoButtons?: boolean;
};

/** Selfcare's Ending Page */
export default ({
  minHeight,
  description,
  onButtonClick,
  onSecondButtonClick,
  icon,
  title,
  buttonLabel,
  variantTitle,
  variantDescription,
  paragraph,
  isParagraphPresent,
  haveTwoButtons = false,
  secondButtonLabel,
  variantFirstButton = 'contained',
  variantSecondButton = 'contained',
}: Props) => (
  <Box sx={{ minHeight, position: 'static' }} display="flex" flexGrow={1}>
    <Grid container direction="column" key="0" style={{ textAlign: 'center' }} margin={'auto'}>
      <Grid container item justifyContent="center" mb={3}>
        <Grid item xs={6}>
          {icon as ReactElement}
        </Grid>
      </Grid>
      <Grid container item justifyContent="center">
        <Grid item xs={6}>
          <Typography variant={variantTitle}>{title}</Typography>
        </Grid>
      </Grid>
      <Grid container item justifyContent="center" mb={4} mt={1}>
        <Grid item xs={6}>
          <Typography variant={variantDescription}>{description}</Typography>
        </Grid>
      </Grid>
      {buttonLabel && (
        <Grid container item justifyContent="center">
          <Grid item xs={haveTwoButtons ? 12 : 4}>
            <Button
              variant={variantFirstButton}
              sx={{ alignSelf: 'center', marginRight: haveTwoButtons ? 3 : 0 }}
              onClick={onButtonClick}
            >
              {buttonLabel}
            </Button>
            {haveTwoButtons && (
              <Button
                variant={variantSecondButton}
                sx={{ alignSelf: 'center' }}
                onClick={onSecondButtonClick}
              >
                {secondButtonLabel}
              </Button>
            )}
          </Grid>
        </Grid>
      )}

      {isParagraphPresent && (
        <Grid container item justifyContent="center" my={4}>
          <Grid item xs={6}>
            <Typography variant={variantDescription}>{paragraph}</Typography>
          </Grid>
        </Grid>
      )}
    </Grid>
  </Box>
);
