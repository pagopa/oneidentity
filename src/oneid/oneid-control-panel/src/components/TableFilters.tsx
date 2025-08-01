import { Clear, Search } from '@mui/icons-material';
import {
  IconButton,
  InputAdornment,
  MenuItem,
  Stack,
  TextField,
} from '@mui/material';

type FilterProps<TOrderField extends string = string> = {
  search?: {
    value: string;
    onChange: (value: string) => void;
  };
  order?: {
    value: TOrderField;
    onChange: (value: TOrderField) => void;
    options?: Array<TOrderField>;
  };
};

const TableFilters = <TOrderField extends string = string>({
  search,
  order,
}: FilterProps<TOrderField>) => (
  <Stack direction="row" spacing={2} alignItems="center">
    {order && (
      <TextField
        select
        sx={{ flex: 1 }}
        label="Order by"
        value={order.value}
        onChange={(e) => order.onChange(e.target.value as TOrderField)}
      >
        {order.options?.map((field) => (
          <MenuItem key={field} value={field}>
            {field.charAt(0).toUpperCase() + field.slice(1)}
          </MenuItem>
        ))}
      </TextField>
    )}
    {search && (
      <TextField
        sx={{ flex: 1 }}
        id="outlined-basic"
        label={'Username'}
        variant="outlined"
        placeholder={'Search...'}
        value={search.value}
        onChange={(e) => search.onChange(e.target.value)}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <Search />
            </InputAdornment>
          ),
          endAdornment: search.value && (
            <InputAdornment position="end">
              <IconButton
                size="small"
                onClick={() => search.onChange('')}
                edge="end"
              >
                <Clear fontSize="small" />
              </IconButton>
            </InputAdornment>
          ),
        }}
      />
    )}
  </Stack>
);

export default TableFilters;
