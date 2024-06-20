import React from 'react';
import ReactDOM from 'react-dom/client';
import { ThemeProvider } from '@mui/material/styles';
import { theme } from '@pagopa/mui-italia';
// import { CONFIG } from '@pagopa/selfcare-common-frontend/config/env';
// import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import App from './App';
// import { ENV } from './utils/env';
import './locale';
// import Root from "./routes/root";

// const router = createBrowserRouter([
//   {
//     path: '/mario',
//     element: <Root />,
//   },
// ]);

const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);
root.render(
  <ThemeProvider theme={theme}>
    <React.StrictMode>
      {/* <RouterProvider router={router} /> */}
      <App />
    </React.StrictMode>
  </ThemeProvider>
);
