import { renderHook, act } from '@testing-library/react';
import { ClientIdProvider, useClientId } from './ClientIdContext';
import * as Storage from '../utils/storage';
import { sessionStorageClientIdKey } from '../utils/constants';

describe('ClientIdContext', () => {
  afterEach(() => {
    // Clear session storage after each test
    sessionStorage.clear();
  });

  const storedClientId = 'test-client-id';

  it('should provide initial clientId from storage', () => {
    Storage.storageWrite(sessionStorageClientIdKey, storedClientId, 'string');

    const { result } = renderHook(useClientId, {
      wrapper: ClientIdProvider,
    });

    expect(result.current.clientId).toBe(storedClientId);
  });

  it('should set and update clientId', () => {
    const { result } = renderHook(useClientId, {
      wrapper: ClientIdProvider,
    });

    const newClientId = 'new-client-id';

    act(() => {
      result.current.setClientId(newClientId);
    });

    expect(result.current.clientId).toBe(newClientId);
    expect(Storage.storageRead(sessionStorageClientIdKey, 'string')).toBe(
      newClientId
    );
  });

  it('should clear clientId', () => {
    Storage.storageWrite(sessionStorageClientIdKey, storedClientId, 'string');

    const { result } = renderHook(useClientId, {
      wrapper: ClientIdProvider,
    });

    expect(result.current.clientId).toBe(storedClientId);

    act(() => {
      result.current.clearClientId();
    });

    expect(result.current.clientId).toBeUndefined();
    expect(
      Storage.storageRead(sessionStorageClientIdKey, 'string')
    ).toBeUndefined();
  });

  it('should handle setting an undefined clientId', () => {
    Storage.storageWrite(sessionStorageClientIdKey, storedClientId, 'string');

    const { result } = renderHook(useClientId, {
      wrapper: ClientIdProvider,
    });

    expect(result.current.clientId).toBe(storedClientId);

    act(() => {
      result.current.setClientId(undefined);
    });

    expect(result.current.clientId).toBeUndefined();
    expect(
      Storage.storageRead(sessionStorageClientIdKey, 'string')
    ).toBeUndefined();
  });
});
