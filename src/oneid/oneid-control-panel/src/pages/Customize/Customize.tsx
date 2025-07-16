import React, { useState, useMemo, useEffect } from 'react';
import {
  Container,
  Typography,
  Paper,
  Box,
  TextField,
  Button,
  Modal,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  SelectChangeEvent,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Divider,
} from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import {
  allLanguages,
  ClientFE,
  ClientFEErrors,
  ClientLocalizedEntry,
  ClientThemeEntry,
  Languages,
} from '../../types/api';
import { useClient } from '../../hooks/useClient';
import { useParams } from 'react-router-dom';
import { LocalizedContentEditor } from './components/LocalizedContentEditor';
import { ThemeManager } from './components/ThemeManager';
import { ClientSettings } from './components/ClientSettings';
import { Notify } from '../../components/Notify';

function CustomizeDashboard() {
  const { client_id: clientID } = useParams(); // Get the client_id from the URL
  const {
    getAdditionalClientAttrs: {
      data: fetchedAdditionalAttributes,
      isSuccess: isFetched,
      error: fetchError,
    },
    createOrUpdateClientAttrsMutation: {
      mutate: updateClientAttrs,
      error: updateError,
      isPending: isUpdating,
      data: clientUpdated,
    },
  } = useClient();

  const [clientData, setClientData] = useState<ClientFE | null>(null);
  const [activeThemeKey, setActiveThemeKey] = useState<string>('');
  const [errorUi, setErrorUi] = useState<ClientFEErrors | null>(null);
  const [notify, setNotify] = useState<Notify>({ open: false });

  useEffect(() => {
    if (isFetched) {
      setClientData(fetchedAdditionalAttributes || null);
      setActiveThemeKey(
        Object.keys(
          fetchedAdditionalAttributes?.localizedContentMap || {}
        )[0] || ''
      );
    }
  }, [fetchError, fetchedAdditionalAttributes, isFetched]);

  const activeTheme = clientData?.localizedContentMap?.[activeThemeKey];
  const activeLanguages: Array<keyof typeof allLanguages> = activeTheme
    ? (Object.keys(activeTheme) as Array<keyof typeof allLanguages>)
    : [];

  const [activeTab, setActiveTab] = useState<string>(activeLanguages[0] || '');

  // Modals state
  const [isLangModalOpen, setLangModalOpen] = useState(false);
  const [isThemeModalOpen, setThemeModalOpen] = useState(false);
  const [isConfirmModalOpen, setConfirmModalOpen] = useState(false);

  // Modal inputs state
  const [languageToAdd, setLanguageToAdd] = useState('');
  const [newThemeKey, setNewThemeKey] = useState('');

  // --- Effects ---
  useEffect(() => {
    const newActiveLanguages = activeTheme ? Object.keys(activeTheme) : [];
    if (!newActiveLanguages.includes(activeTab)) {
      setActiveTab(newActiveLanguages[0] || '');
    }
  }, [activeThemeKey, activeTheme, activeTab]);

  useEffect(() => {
    if (updateError) {
      console.error('Error updating client:', updateError);
      setErrorUi(updateError as unknown as ClientFEErrors);
      setNotify({
        open: true,
        message: 'Error updating client',
        severity: 'error',
      });
    }
    if (clientUpdated) {
      setErrorUi(null);
      setNotify({
        open: true,
        message: 'Client updated successfully',
        severity: 'success',
      });
    }
  }, [updateError, clientUpdated]);

  // --- Handlers ---
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!clientData && !isFormValid()) {
      console.error('Form is not valid, please check the data');
      return;
    }

    updateClientAttrs({
      data: clientData as ClientFE,
    });
  };

  const isFormValid = () => {
    if (!clientData) return false;
    // Check if at least one theme exists
    if (
      !clientData.localizedContentMap ||
      Object.keys(clientData.localizedContentMap).length === 0
    ) {
      console.error('At least one theme is required');
      return false;
    }
    // Check if the 'default' theme exists
    if (!clientData.localizedContentMap.default) {
      console.error('The "default" theme is required');
      return false;
    }
    // Check if each theme has at least one language with title and description
    for (const [themeKey, themeContent] of Object.entries(
      clientData.localizedContentMap
    )) {
      if (!themeContent || Object.keys(themeContent).length === 0) {
        console.error(`Theme "${themeKey}" must have at least one language`);
        return false;
      }
    }
    return true;
  };

  const handleTopLevelChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = event.target;
    setClientData((prev) => {
      if (!prev) prev = {} as ClientFE; // Ensure prev is always defined
      // Always set backButtonEnabled explicitly and ensure it's always boolean
      return {
        ...prev,
        [name]: type === 'checkbox' ? checked : value,
        backButtonEnabled:
          name === 'backButtonEnabled'
            ? checked
            : prev?.backButtonEnabled !== undefined
              ? prev.backButtonEnabled
              : false,
      };
    });
  };

  const handleLocalizedContentChange = (
    lang: Languages,
    field: keyof ClientThemeEntry,
    value: string
  ) => {
    setClientData((prev) => {
      // Guard against updating a non-existent theme to prevent runtime errors.
      if (!prev?.localizedContentMap?.[activeThemeKey]) {
        return prev;
      }

      const newClientData: ClientFE = {
        ...prev,
        localizedContentMap: {
          ...prev.localizedContentMap,
          [activeThemeKey]: {
            ...prev.localizedContentMap[activeThemeKey],
            [lang]: {
              // Safely spread the existing language entry, providing a default shape if it's missing.
              ...(prev.localizedContentMap[activeThemeKey][lang] || {
                title: '',
                description: '',
              }),
              [field]: value,
            },
          },
        },
      };
      return newClientData;
    });
  };

  const handleAddLanguage = () => {
    if (!languageToAdd) return;
    setClientData((prev) => {
      if (!prev || !prev.localizedContentMap) return prev;
      const newThemeContent = {
        ...prev.localizedContentMap[activeThemeKey],
        [languageToAdd]: { title: '', description: '' },
      };
      return {
        ...prev,
        localizedContentMap: {
          ...prev.localizedContentMap,
          [activeThemeKey]: newThemeContent,
        },
      };
    });
    setActiveTab(languageToAdd);
    setLangModalOpen(false);
  };

  const handleRemoveLanguage = (langToRemove: string) => {
    if (!activeTheme || Object.keys(activeTheme).length <= 1) return;
    setClientData((prev) => {
      if (!prev || !prev.localizedContentMap) return prev;
      const themeContent = prev.localizedContentMap[activeThemeKey] as Record<
        string,
        ClientLocalizedEntry
      >;
      const newThemeContent = Object.keys(themeContent)
        .filter((lang) => lang !== langToRemove)
        .reduce(
          (acc, lang) => {
            return { ...acc, [lang]: themeContent[lang] };
          },
          {} as Record<string, ClientLocalizedEntry>
        );
      return {
        ...prev,
        localizedContentMap: {
          ...prev.localizedContentMap,
          [activeThemeKey]: newThemeContent,
        },
      };
    });
  };
  const defaultThemeExists = useMemo(
    () =>
      !!clientData &&
      !!clientData.localizedContentMap &&
      Object.prototype.hasOwnProperty.call(
        clientData.localizedContentMap,
        'default'
      ),
    [clientData]
  );

  // force user to create at least one theme called "default"
  const handleAddTheme = () => {
    // If 'default' doesn't exist, the new theme MUST be 'default'.
    // Otherwise, create a key from the user's input.
    const key = !defaultThemeExists
      ? 'default'
      : newThemeKey.trim().toLowerCase().replace(/\s+/g, '-');

    // Prevent adding a theme if the key is empty or already exists.
    if (!key || clientData?.localizedContentMap?.[key]) {
      return;
    }

    setClientData((prev) => ({
      ...prev,
      backButtonEnabled: false,
      a11yUri: null,
      localizedContentMap: {
        ...prev?.localizedContentMap,
        [key]: {
          it: {
            title: '',
            desc: '',
          },
        },
      },
    }));
    setActiveThemeKey(key);
    setNewThemeKey('');
    setThemeModalOpen(false);
  };

  const handleConfirmRemoveTheme = () => {
    // Prevent deleting the 'default' theme or the last remaining theme.
    if (
      activeThemeKey === 'default' ||
      Object.keys(clientData?.localizedContentMap || {}).length <= 1
    ) {
      setConfirmModalOpen(false);
      return;
    }
    setClientData((prev) => {
      if (!prev?.localizedContentMap) return prev;
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { [activeThemeKey]: _, ...newMap } = prev.localizedContentMap;
      setActiveThemeKey(Object.keys(newMap)[0] || '');
      return { ...prev, localizedContentMap: newMap };
    });
    setConfirmModalOpen(false);
  };

  const availableLanguages = useMemo(() => {
    if (!activeTheme) return [];
    return Object.entries(allLanguages)
      .filter(
        ([code]) => !Object.prototype.hasOwnProperty.call(activeTheme, code)
      )
      .map(([code, name]) => ({ code, name }));
  }, [activeTheme]);

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Paper elevation={2} sx={{ borderRadius: 3, overflow: 'hidden' }}>
        <Box p={{ xs: 2, sm: 3, md: 4 }}>
          <Typography variant="h4" component="h1" fontWeight="bold">
            Client Configuration
          </Typography>
          <Typography variant="body1" color="text.secondary" mb={4}>
            Manage the client settings and localized content.
          </Typography>

          <ClientSettings
            clientData={clientData}
            clientID={clientID}
            onClientDataChange={handleTopLevelChange}
            errorUi={errorUi}
          />

          <Divider sx={{ my: 4 }} />

          <ThemeManager
            themes={clientData?.localizedContentMap}
            activeThemeKey={activeThemeKey}
            onThemeChange={(e) => setActiveThemeKey(e.target.value)}
            onAddTheme={() => setThemeModalOpen(true)}
            onRemoveTheme={() => setConfirmModalOpen(true)}
            errorUi={errorUi}
          />

          {activeTheme && (
            <LocalizedContentEditor
              activeLanguages={activeLanguages}
              activeTheme={activeTheme}
              activeThemeKey={activeThemeKey}
              activeTab={activeTab}
              onTabChange={(_e, val) => setActiveTab(val)}
              onContentChange={handleLocalizedContentChange}
              onAddLanguage={() => setLangModalOpen(true)}
              onRemoveLanguage={handleRemoveLanguage}
              errorUi={errorUi}
            />
          )}
        </Box>

        <Box
          sx={{
            p: 2,
            bgcolor: 'grey.50',
            borderTop: 1,
            borderColor: 'grey.200',
            display: 'flex',
            justifyContent: 'flex-end',
            gap: 2,
          }}
        >
          <Button variant="outlined">Cancel</Button>
          <Button
            variant="contained"
            startIcon={<SaveIcon />}
            onClick={handleSubmit}
            disabled={isUpdating || !isFormValid()}
          >
            {isUpdating ? 'Saving...' : 'Save Changes'}
          </Button>
        </Box>
      </Paper>

      {/* TODO swith to useModal hook */}
      {/* Modals are unchanged but their handlers are updated */}
      <Modal open={isLangModalOpen} onClose={() => setLangModalOpen(false)}>
        <Box
          sx={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            width: 400,
            bgcolor: 'background.paper',
            boxShadow: 24,
            borderRadius: 2,
            p: 4,
          }}
        >
          <Typography variant="h6" component="h2" mb={2}>
            Add Language
          </Typography>
          <LanguageSelector
            value={languageToAdd}
            onChange={(e) => setLanguageToAdd(e.target.value)}
            available={availableLanguages}
          />
          <ModalActions
            onCancel={() => setLangModalOpen(false)}
            onConfirm={handleAddLanguage}
            confirmText="Add"
          />
        </Box>
      </Modal>

      <Modal open={isThemeModalOpen} onClose={() => setThemeModalOpen(false)}>
        <Box
          sx={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
            width: 400,
            bgcolor: 'background.paper',
            boxShadow: 24,
            borderRadius: 2,
            p: 4,
          }}
        >
          <Typography variant="h6" component="h2" mb={2}>
            Add New Theme Key
          </Typography>
          <TextField
            label={
              !defaultThemeExists
                ? 'Theme Key'
                : "New Theme Key (e.g. 'marketing')"
            }
            value={!defaultThemeExists ? 'default' : newThemeKey}
            onChange={(e) => setNewThemeKey(e.target.value)}
            fullWidth
            autoFocus
            disabled={!defaultThemeExists}
          />
          <ModalActions
            onCancel={() => setThemeModalOpen(false)}
            onConfirm={handleAddTheme}
            confirmText="Create"
          />
        </Box>
      </Modal>
      <Dialog
        open={isConfirmModalOpen}
        onClose={() => setConfirmModalOpen(false)}
      >
        <DialogTitle>Delete Theme?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete the theme &quot;{activeThemeKey}
            &quot;? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmModalOpen(false)}>Cancel</Button>
          <Button onClick={handleConfirmRemoveTheme} color="error">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
      <Notify
        open={notify.open}
        message={notify.message}
        severity={notify.severity}
        handleOpen={(open) => setNotify({ ...notify, open })}
      />
    </Container>
  );
}

// --- Helper Components for Modals (unchanged) ---
const ModalActions: React.FC<{
  onCancel: () => void;
  onConfirm: () => void;
  confirmText: string;
}> = ({ onCancel, onConfirm, confirmText }) => (
  <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
    <Button onClick={onCancel}>Cancel</Button>
    <Button variant="contained" onClick={onConfirm}>
      {confirmText}
    </Button>
  </Box>
);
const LanguageSelector: React.FC<{
  value: string;
  onChange: (e: SelectChangeEvent<string>) => void;
  available: Array<{ code: string; name: string }>;
}> = ({ value, onChange, available }) => (
  <FormControl fullWidth>
    <InputLabel>Available Languages</InputLabel>
    <Select
      value={value}
      label="Available Languages"
      onChange={onChange}
      data-testid="language-selector"
    >
      {available.map(({ code, name }) => (
        <MenuItem key={code} value={code}>
          {name}
        </MenuItem>
      ))}
    </Select>
  </FormControl>
);

export const Customize = () => {
  return (
    <Box sx={{ bgcolor: 'grey.50', minHeight: '100vh' }}>
      <CustomizeDashboard />
    </Box>
  );
};
