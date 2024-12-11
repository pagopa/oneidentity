import React from 'react';
import { createRoot } from 'react-dom/client';
import { ThemeProvider } from '@mui/material/styles';
import { theme } from '@pagopa/mui-italia';

import App from './App';
import './locale';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

const rootEl = document.getElementById('root');

if (rootEl) {
  const root = createRoot(rootEl);
  const queryClient = new QueryClient();

  root.render(
    <ThemeProvider theme={theme}>
      <React.StrictMode>
        <QueryClientProvider client={queryClient}>
          <App />
        </QueryClientProvider>
      </React.StrictMode>
    </ThemeProvider>
  );
}
