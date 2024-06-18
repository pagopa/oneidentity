import React from 'react';
import ReactDOM from 'react-dom/client';
import { ThemeProvider } from '@mui/material/styles';
import { theme } from '@pagopa/mui-italia';
// import { CONFIG } from '@pagopa/selfcare-common-frontend/config/env';
// import App from './App';
// import { ENV } from './utils/env';
import './locale';


const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);
root.render(
  <ThemeProvider theme={theme}>
    <React.StrictMode>
      {/* <App /> */}
    </React.StrictMode>
  </ThemeProvider>
);
