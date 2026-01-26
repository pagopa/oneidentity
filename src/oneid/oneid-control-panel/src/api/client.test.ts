import { describe, it, expect, vi, afterEach } from 'vitest';
import {
  getClientUsers,
  deleteClientUser,
  updateClientUser,
  addClientUser,
} from './client';
import { ENV } from '../utils/env';
import { IdpUser, IdpUserList, SamlAttribute } from '../types/api';

vi.mock('../utils/env', () => ({
  ENV: {
    URL_API: {
      CLIENT_USERS: 'https://api.example.com/client-users',
    },
  },
}));

vi.mock('../utils/errors', () => ({
  handleApiError: vi.fn((error) => error),
}));

const axiosMock = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  patch: vi.fn(),
  delete: vi.fn(),
}));

vi.mock('axios', async (importActual) => {
  const actual = await importActual<typeof import('axios')>();

  return {
    default: {
      ...actual.default,
      isAxiosError: vi.fn((payload) => !!payload?.isAxiosError),
      create: vi.fn(() => ({
        ...actual.default.create(),
        get: axiosMock.get,
        post: axiosMock.post,
        put: axiosMock.put,
        patch: axiosMock.patch,
        delete: axiosMock.delete,
        interceptors: {
          request: { use: vi.fn(), eject: vi.fn() },
          response: { use: vi.fn(), eject: vi.fn() },
        },
      })),
    },
  };
});

describe('client api', () => {
  const mockUser: IdpUser = {
    username: 'testuser',
    password: 'password123',
    samlAttributes: {
      [SamlAttribute.EMAIL]: 'test@example.com',
      [SamlAttribute.FAMILY_NAME]: 'Test',
      [SamlAttribute.GIVEN_NAME]: 'User',
    },
  };

  const mockUserList: IdpUserList = {
    users: [mockUser],
  };

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('getClientUsers', () => {
    it('fetches client users successfully', async () => {
      axiosMock.get.mockResolvedValueOnce({ data: mockUserList });

      const result = await getClientUsers();

      expect(result).toEqual(mockUserList);
      expect(axiosMock.get).toHaveBeenCalledWith(
        `${ENV.URL_API.CLIENT_USERS}?limit=1000`
      );
    });

    it('throws error when fetch fails', async () => {
      const mockError = new Error('Fetch failed');
      axiosMock.get.mockRejectedValueOnce(mockError);

      await expect(getClientUsers()).rejects.toThrow('Fetch failed');
    });
  });

  describe('deleteClientUser', () => {
    it('deletes a client user successfully', async () => {
      axiosMock.delete.mockResolvedValueOnce({});

      await deleteClientUser('testuser');

      expect(axiosMock.delete).toHaveBeenCalledWith(
        `${ENV.URL_API.CLIENT_USERS}/testuser`
      );
    });

    it('throws error if username is missing', async () => {
      await expect(deleteClientUser(undefined)).rejects.toThrow(
        'User Name is required'
      );
    });

    it('throws error when delete fails', async () => {
      const mockError = new Error('Delete failed');
      axiosMock.delete.mockRejectedValueOnce(mockError);

      await expect(deleteClientUser('testuser')).rejects.toThrow(
        'Delete failed'
      );
    });
  });

  describe('updateClientUser', () => {
    it('updates a client user successfully', async () => {
      axiosMock.patch.mockResolvedValueOnce({ data: { message: 'Updated' } });

      const result = await updateClientUser('testuser', mockUser);

      expect(result).toEqual({ message: 'Updated' });
      expect(axiosMock.patch).toHaveBeenCalledWith(
        `${ENV.URL_API.CLIENT_USERS}/testuser`,
        mockUser
      );
    });

    it('throws error if username is missing', async () => {
      await expect(updateClientUser(undefined, mockUser)).rejects.toThrow(
        'User Name is required'
      );
    });

    it('throws error when update fails', async () => {
      const mockError = new Error('Update failed');
      axiosMock.patch.mockRejectedValueOnce(mockError);

      await expect(updateClientUser('testuser', mockUser)).rejects.toThrow(
        'Update failed'
      );
    });
  });

  describe('addClientUser', () => {
    it('adds a client user successfully', async () => {
      axiosMock.post.mockResolvedValueOnce({ data: { message: 'Created' } });

      const result = await addClientUser(mockUser);

      expect(result).toEqual({ message: 'Created' });
      expect(axiosMock.post).toHaveBeenCalledWith(
        `${ENV.URL_API.CLIENT_USERS}`,
        mockUser
      );
    });

    it('throws error if data is missing', async () => {
      await expect(
        addClientUser(undefined as unknown as IdpUser)
      ).rejects.toThrow('Data is required');
    });

    it('throws "User already exists" on 409 error', async () => {
      const axiosError = {
        isAxiosError: true,
        response: { status: 409 },
      };
      axiosMock.post.mockRejectedValueOnce(axiosError);

      await expect(addClientUser(mockUser)).rejects.toThrow(
        'User already exists'
      );
    });

    it('throws generic error when addition fails', async () => {
      const mockError = new Error('Add failed');
      axiosMock.post.mockRejectedValueOnce(mockError);

      await expect(addClientUser(mockUser)).rejects.toThrow('Add failed');
    });
  });
});
