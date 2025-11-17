import React from 'react';
import { createRoot } from 'react-dom/client';
import { ThemeProvider } from '@mui/material/styles';
import { theme } from '@pagopa/mui-italia';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ServiceKO } from '../pages/courtesy/ServiceKO';
import '../locale';
import '../global.css';

const rootEl = document.getElementById('root');

if (rootEl) {
  const root = createRoot(rootEl);
  const queryClient = new QueryClient();

  root.render(
    <ThemeProvider theme={theme}>
      <React.StrictMode>
        <QueryClientProvider client={queryClient}>
          <ServiceKO />
        </QueryClientProvider>
      </React.StrictMode>
    </ThemeProvider>
  );
}
