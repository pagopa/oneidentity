import React from 'react';
import {
  Typography,
  Box,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  IconButton,
  Grid,
  SelectChangeEvent,
  Tooltip,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import { ClientLocalizedEntry } from '../../../types/api';

type ThemeManagerProps = {
  themes: ClientLocalizedEntry | undefined | null;
  activeThemeKey: string;
  onThemeChange: (event: SelectChangeEvent<string>) => void;
  onAddTheme: () => void;
  onRemoveTheme: () => void;
};

export const ThemeManager: React.FC<ThemeManagerProps> = ({
  themes,
  activeThemeKey,
  onThemeChange,
  onAddTheme,
  onRemoveTheme,
}) => (
  <Box mb={3}>
    <Typography variant="h5" component="h2" fontWeight="bold" mb={2}>
      Localized Content Themes
    </Typography>
    <Grid container spacing={2} alignItems="center" sx={{ mb: 3 }}>
      <Grid item xs>
        <FormControl fullWidth>
          <InputLabel>Active Theme</InputLabel>
          <Select
            value={activeThemeKey}
            label="Active Theme"
            onChange={onThemeChange}
            aria-label="active theme"
            data-testid="theme-select"
          >
            {Object.keys(themes || {}).map((key) => (
              <MenuItem key={key} value={key}>
                {key}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Grid>
      <Grid item xs="auto">
        <Tooltip title="Add New Theme">
          <IconButton color="primary" onClick={onAddTheme}>
            <AddIcon />
          </IconButton>
        </Tooltip>
      </Grid>
      <Grid item xs="auto">
        <Tooltip title="Delete Current Theme">
          <span>
            <IconButton
              onClick={onRemoveTheme}
              aria-label="Delete Current Theme"
              disabled={
                activeThemeKey === 'default' ||
                Object.keys(themes || {}).length <= 1
              }
              sx={{ color: 'error.main' }}
            >
              <DeleteIcon />
            </IconButton>
          </span>
        </Tooltip>
      </Grid>
    </Grid>
  </Box>
);
