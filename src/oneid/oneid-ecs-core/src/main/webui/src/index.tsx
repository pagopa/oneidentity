import React from 'react';
import { createRoot } from 'react-dom/client';
import { ThemeProvider } from '@mui/material/styles';
import { theme } from '@pagopa/mui-italia';

import App from './App';
import './locale';

const rootEl = document.getElementById('root');

if (rootEl) {
  const root = createRoot(rootEl);

  root.render(
    <ThemeProvider theme={theme}>
      <React.StrictMode>
        <App />
      </React.StrictMode>
    </ThemeProvider>
  );
}
