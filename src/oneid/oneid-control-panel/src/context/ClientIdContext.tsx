import React, { createContext, useContext, useState } from 'react';
import * as Storage from '../utils/storage';
import { sessionStorageClientIdKey } from '../utils/constants';

type ClientIdContextType = {
  clientId?: string;
  setClientId: (id?: string) => void;
  clearClientId: () => void;
};

const ClientIdContext = createContext<ClientIdContextType>({
  clientId: undefined,
  setClientId: () => null,
  clearClientId: () => null,
});

type ClientIdProviderProps = {
  children: React.ReactNode;
};

export const ClientIdProvider = ({ children }: ClientIdProviderProps) => {
  const [clientId, setClientIdState] = useState<string | undefined>(
    Storage.storageRead(sessionStorageClientIdKey, 'string')
  );

  const setClientId = (id?: string) => {
    if (id) {
      Storage.storageWrite(sessionStorageClientIdKey, id, 'string');
    } else {
      Storage.storageDelete(sessionStorageClientIdKey);
    }
    setClientIdState(id);
  };

  const clearClientId = () => {
    Storage.storageDelete(sessionStorageClientIdKey);
    setClientIdState(undefined);
  };

  return (
    <ClientIdContext.Provider value={{ clientId, setClientId, clearClientId }}>
      {children}
    </ClientIdContext.Provider>
  );
};

export const useClientId = () => useContext(ClientIdContext);
