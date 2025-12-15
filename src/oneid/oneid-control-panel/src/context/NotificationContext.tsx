import React, { createContext, useContext, useState, useCallback } from 'react';
import { Notify } from '../components/Notify';

type NotificationSeverity = 'success' | 'error';

type NotificationContextType = {
  showNotification: (message: string, severity?: NotificationSeverity) => void;
};

const NotificationContext = createContext<NotificationContextType | undefined>(
  undefined
);

type NotificationProviderProps = {
  children: React.ReactNode;
};

export const NotificationProvider = ({
  children,
}: NotificationProviderProps) => {
  const [notify, setNotify] = useState<{
    open: boolean;
    message?: string;
    severity?: NotificationSeverity;
  }>({ open: false });

  const showNotification = useCallback(
    (message: string, severity: NotificationSeverity = 'success') => {
      setNotify({
        open: true,
        message,
        severity,
      });
    },
    []
  );

  const handleClose = useCallback((open: boolean) => {
    setNotify((prev) => ({ ...prev, open }));
  }, []);

  return (
    <NotificationContext.Provider value={{ showNotification }}>
      {children}
      <Notify
        open={notify.open}
        message={notify.message}
        severity={notify.severity}
        handleOpen={handleClose}
      />
    </NotificationContext.Provider>
  );
};

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (context === undefined) {
    throw new Error(
      'useNotification must be used within a NotificationProvider'
    );
  }
  return context;
};
