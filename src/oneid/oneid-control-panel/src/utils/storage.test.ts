import { storageDelete, storageRead, storageWrite } from './storage';

const key = 'testKey';

describe('storage', () => {
  afterEach(() => {
    window.sessionStorage.clear();
    window.localStorage.clear();
  });

  describe('storageWrite', () => {
    it('should write a string to session storage', () => {
      const value = 'testValue';
      storageWrite(key, value, 'string');
      expect(window.sessionStorage.getItem(key)).toBe(value);
    });

    it('should write a number to session storage', () => {
      const value = 123;
      storageWrite(key, value, 'number');
      expect(window.sessionStorage.getItem(key)).toBe(String(value));
    });

    it('should write an object to session storage', () => {
      const value = { a: 1 };
      storageWrite(key, value, 'object');
      expect(window.sessionStorage.getItem(key)).toBe(JSON.stringify(value));
    });

    it('should write to local storage if local is true', () => {
      const value = 'testValue';
      storageWrite(key, value, 'string', true);
      expect(window.localStorage.getItem(key)).toBe(value);
    });
  });

  describe('storageRead', () => {
    it('should read a string from session storage', () => {
      const value = 'testValue';
      window.sessionStorage.setItem(key, value);
      const result = storageRead(key, 'string');
      expect(result).toBe(value);
    });

    it('should read a number from session storage', () => {
      const value = 123;
      window.sessionStorage.setItem(key, String(value));
      const result = storageRead(key, 'number');
      expect(result).toBe(value);
    });

    it('should read an object from session storage', () => {
      const value = { a: 1 };
      window.sessionStorage.setItem(key, JSON.stringify(value));
      const result = storageRead(key, 'object');
      expect(result).toEqual(value);
    });

    it('should read from local storage if local is true', () => {
      const value = 'testValue';
      window.localStorage.setItem(key, value);
      const result = storageRead(key, 'string', true);
      expect(result).toBe(value);
    });

    it('should return undefined if key does not exist', () => {
      const result = storageRead('nonExistentKey', 'string');
      expect(result).toBeUndefined();
    });
  });

  describe('storageDelete', () => {
    it('should delete a key from session storage', () => {
      const value = 'testValue';
      window.sessionStorage.setItem(key, value);
      storageDelete(key);
      expect(window.sessionStorage.getItem(key)).toBeNull();
    });

    it('should delete a key from local storage if local is true', () => {
      const value = 'testValue';
      window.localStorage.setItem(key, value);
      storageDelete(key, true);
      expect(window.localStorage.getItem(key)).toBeNull();
    });
  });
});
