import { TextField, Select } from '@mui/material';
import React, { ReactNode } from 'react';
import { Box, IconButton, Tooltip } from '@mui/material';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';

const infoIconButtonSx = {
  color: 'text.secondary',
  p: 0.5,
  '&:hover': { color: 'primary.main' },
};

type Placement =
  | 'right'
  | 'left'
  | 'top'
  | 'bottom'
  | 'top-end'
  | 'bottom-end'
  | 'top-start'
  | 'bottom-start';

// Type guard for ReactElement valid element with known props (TextField and Select)
function isReactElementWithProps<
  P = { InputProps?: object; endAdornment?: object },
>(el: ReactNode): el is React.ReactElement<P> {
  return React.isValidElement(el);
}

type FieldWithInfoProps = {
  children: ReactNode;
  tooltipText: string | ReactNode; // tooltipText can be either a string -> it will be displayed as plain text inside the tooltip; or a ReactNode -> it will be rendered directly as JSX/HTML
  placement?: Placement;
  inputAdornment?: boolean;
};

const FieldWithInfo: React.FC<FieldWithInfoProps> = ({
  children,
  tooltipText,
  placement = 'top',
  inputAdornment = false, // for TextField or Select
}) => {
  // tooltip or popover popup
  const infoPopup = (
    <Tooltip title={tooltipText} arrow placement={placement}>
      <span>
        <IconButton
          size="small"
          tabIndex={0}
          sx={infoIconButtonSx}
          data-testid="info-icon"
        >
          <InfoOutlinedIcon fontSize="small" />
        </IconButton>
      </span>
    </Tooltip>
  );

  // if inputAdornment is true build info popup inside the input field (on the right)
  if (inputAdornment && isReactElementWithProps(children)) {
    const isTextField = children.type === TextField;
    const isSelect = children.type === Select;

    // case 1 = children is a TextField
    if (isTextField) {
      const prevProps = children.props.InputProps ?? {};
      return React.cloneElement(children, {
        InputProps: {
          ...prevProps,
          endAdornment: infoPopup,
        },
      });
    }

    // case 2 = children is a Select
    if (isSelect) {
      const prevProps = children.props ?? {};
      return React.cloneElement(children, {
        ...prevProps,
        endAdornment: (
          <>
            <Box sx={{ marginRight: 3 }}>{infoPopup}</Box>
            {children.props.endAdornment}
          </>
        ),
      });
    }
  }

  // if inputAdornment is false (or children type isn't one of TextField or Select)
  // build info popup outside the component, to the right
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        flexWrap: 'wrap',
        minWidth: 200,
        flex: 1,
      }}
    >
      {children}
      {infoPopup}
    </Box>
  );
};

export default FieldWithInfo;
