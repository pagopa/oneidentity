import React, { useMemo } from 'react';
import {
  Typography,
  Box,
  TextField,
  Button,
  Tabs,
  Tab,
  IconButton,
  Grid,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import {
  allLanguages,
  ClientFEErrors,
  ClientLocalizedEntry,
  ClientThemeEntry,
  Languages,
} from '../../../types/api';

type LocalizedContentEditorProps = {
  activeLanguages: Array<Languages>;
  activeTheme: ClientLocalizedEntry;
  activeThemeKey: string;
  activeTab: string;
  onTabChange: (event: React.SyntheticEvent, newValue: string) => void;
  onContentChange: (
    lang: Languages,
    field: keyof ClientThemeEntry,
    value: string
  ) => void;
  onAddLanguage: () => void;
  onRemoveLanguage: (lang: string) => void;
  errorUi: ClientFEErrors | null;
};

export const LocalizedContentEditor: React.FC<LocalizedContentEditorProps> = ({
  activeLanguages,
  activeTheme,
  activeThemeKey,
  activeTab,
  onTabChange,
  onContentChange,
  onAddLanguage,
  onRemoveLanguage,
  errorUi,
}) => {
  const availableLanguages = useMemo(
    () =>
      Object.keys(allLanguages).filter(
        (lang) => !activeLanguages.includes(lang as Languages)
      ),
    [activeLanguages]
  );

  return (
    <>
      <Box
        display="flex"
        justifyContent="space-between"
        alignItems="center"
        pb={1}
        mb={1}
      >
        <Typography variant="h6" component="h3">
          Languages for &quot;{activeThemeKey}&quot;
        </Typography>
        <Button
          variant="contained"
          size="small"
          startIcon={<AddIcon />}
          onClick={onAddLanguage}
          disabled={availableLanguages.length === 0}
        >
          Add Language
        </Button>
      </Box>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs
          value={activeTab}
          onChange={onTabChange}
          variant="scrollable"
          scrollButtons="auto"
        >
          {activeLanguages.map((lang) => (
            <Tab
              key={lang}
              value={lang}
              label={
                <Box
                  component="span"
                  sx={{ display: 'flex', alignItems: 'center' }}
                >
                  {allLanguages[lang] || lang}
                  <IconButton
                    component="span"
                    size="small"
                    sx={{ ml: 1.5 }}
                    onClick={(e) => {
                      e.stopPropagation();
                      onRemoveLanguage(lang);
                    }}
                    disabled={activeLanguages.length <= 1}
                  >
                    <CloseIcon sx={{ fontSize: '1rem' }} />
                  </IconButton>
                </Box>
              }
            />
          ))}
        </Tabs>
      </Box>
      <Box pt={3}>
        {activeLanguages.map((lang) => (
          <Box key={lang} role="tabpanel" hidden={activeTab !== lang}>
            {activeTab === lang && (
              <Grid container spacing={3}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    label="Title"
                    value={activeTheme[lang]?.title || ''}
                    onChange={(e) =>
                      onContentChange(lang, 'title', e.target.value)
                    }
                    fullWidth
                    error={
                      !!(errorUi as ClientFEErrors)?.localizedContentMap?.[
                        activeThemeKey
                      ]?.[lang]?.title?._errors
                    }
                    helperText={
                      (errorUi as ClientFEErrors)?.localizedContentMap?.[
                        activeThemeKey
                      ]?.[lang]?.title?._errors
                    }
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    label="Support Address"
                    value={activeTheme[lang]?.supportAddress || ''}
                    onChange={(e) =>
                      onContentChange(lang, 'supportAddress', e.target.value)
                    }
                    fullWidth
                    error={
                      !!(errorUi as ClientFEErrors)?.localizedContentMap?.[
                        activeThemeKey
                      ]?.[lang]?.supportAddress?._errors
                    }
                    helperText={
                      (errorUi as ClientFEErrors)?.localizedContentMap?.[
                        activeThemeKey
                      ]?.[lang]?.supportAddress?._errors
                    }
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    label="Description"
                    value={activeTheme[lang]?.desc || ''}
                    onChange={(e) =>
                      onContentChange(lang, 'desc', e.target.value)
                    }
                    multiline
                    rows={4}
                    fullWidth
                    error={
                      !!(errorUi as ClientFEErrors)?.localizedContentMap?.[
                        activeThemeKey
                      ]?.[lang]?.desc?._errors
                    }
                    helperText={
                      (errorUi as ClientFEErrors)?.localizedContentMap?.[
                        activeThemeKey
                      ]?.[lang]?.desc?._errors
                    }
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    label="Documentation URI"
                    value={activeTheme[lang]?.docUri || ''}
                    onChange={(e) =>
                      onContentChange(lang, 'docUri', e.target.value)
                    }
                    fullWidth
                    error={
                      !!(errorUi as ClientFEErrors)?.localizedContentMap?.[
                        activeThemeKey
                      ]?.[lang]?.docUri?._errors
                    }
                    helperText={
                      (errorUi as ClientFEErrors)?.localizedContentMap?.[
                        activeThemeKey
                      ]?.[lang]?.docUri?._errors
                    }
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    label="Cookie Policy URI"
                    value={activeTheme[lang]?.cookieUri || ''}
                    onChange={(e) =>
                      onContentChange(lang, 'cookieUri', e.target.value)
                    }
                    fullWidth
                    error={
                      !!(errorUi as ClientFEErrors)?.localizedContentMap?.[
                        activeThemeKey
                      ]?.[lang]?.cookieUri?._errors
                    }
                    helperText={
                      (errorUi as ClientFEErrors)?.localizedContentMap?.[
                        activeThemeKey
                      ]?.[lang]?.cookieUri?._errors
                    }
                  />
                </Grid>
              </Grid>
            )}
          </Box>
        ))}
      </Box>
    </>
  );
};
