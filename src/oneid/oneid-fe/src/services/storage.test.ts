import { describe, it, vi, expect, Mock } from 'vitest';
import { storageWrite, storageRead, storageDelete } from './storage';

describe('storage utilities', () => {
  // Mock localStorage and sessionStorage
  const mockStorage = {
    setItem: vi.fn(),
    getItem: vi.fn(),
    removeItem: vi.fn(),
    clear: vi.fn(),
    key: vi.fn(),
    length: 0,
  };
  vi.stubGlobal('window', {
    localStorage: mockStorage,
    sessionStorage: mockStorage,
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should store a string value in sessionStorage by default', () => {
    const key = 'testKey';
    const value = 'testValue';

    storageWrite(key, value, 'string');

    expect(window.sessionStorage.setItem).toHaveBeenCalledWith(key, value);
  });

  it('should store a number value in sessionStorage by default', () => {
    const key = 'testKey';
    const value = 42;

    storageWrite(key, value, 'number');

    expect(window.sessionStorage.setItem).toHaveBeenCalledWith(key, '42');
  });

  it('should store an object value in sessionStorage by default', () => {
    const key = 'testKey';
    const value = { foo: 'bar' };

    storageWrite(key, value, 'object');

    expect(window.sessionStorage.setItem).toHaveBeenCalledWith(
      key,
      JSON.stringify(value)
    );
  });

  it('should store a string value in localStorage when local is true', () => {
    const key = 'testKey';
    const value = 'testValue';

    storageWrite(key, value, 'string', true);

    expect(window.localStorage.setItem).toHaveBeenCalledWith(key, value);
  });

  it('should store a number value in localStorage when local is true', () => {
    const key = 'testKey';
    const value = 42;

    storageWrite(key, value, 'number', true);

    expect(window.localStorage.setItem).toHaveBeenCalledWith(key, '42');
  });

  it('should store an object value in localStorage when local is true', () => {
    const key = 'testKey';
    const value = { foo: 'bar' };

    storageWrite(key, value, 'object', true);

    expect(window.localStorage.setItem).toHaveBeenCalledWith(
      key,
      JSON.stringify(value)
    );
  });

  it('should read a string value from sessionStorage by default', () => {
    const key = 'testKey';
    const value = 'testValue';
    (window.sessionStorage.getItem as Mock).mockReturnValue(value);

    const result = storageRead(key, 'string');

    expect(window.sessionStorage.getItem).toHaveBeenCalledWith(key);
    expect(result).toBe(value);
  });

  it('should read a number value from sessionStorage by default', () => {
    const key = 'testKey';
    const value = '42';
    (window.sessionStorage.getItem as Mock).mockReturnValue(value);

    const result = storageRead(key, 'number');

    expect(window.sessionStorage.getItem).toHaveBeenCalledWith(key);
    expect(result).toBe(42);
  });

  it('should read an object value from sessionStorage by default', () => {
    const key = 'testKey';
    const value = JSON.stringify({ foo: 'bar' });
    (window.sessionStorage.getItem as Mock).mockReturnValue(value);

    const result = storageRead(key, 'object');

    expect(window.sessionStorage.getItem).toHaveBeenCalledWith(key);
    expect(result).toEqual({ foo: 'bar' });
  });

  it('should read a string value from localStorage when local is true', () => {
    const key = 'testKey';
    const value = 'testValue';
    (window.localStorage.getItem as Mock).mockReturnValue(value);

    const result = storageRead(key, 'string', true);

    expect(window.localStorage.getItem).toHaveBeenCalledWith(key);
    expect(result).toBe(value);
  });

  it('should delete a key from sessionStorage by default', () => {
    const key = 'testKey';

    storageDelete(key);

    expect(window.sessionStorage.removeItem).toHaveBeenCalledWith(key);
  });

  it('should delete a key from localStorage when local is true', () => {
    const key = 'testKey';

    storageDelete(key, true);

    expect(window.localStorage.removeItem).toHaveBeenCalledWith(key);
  });
});
