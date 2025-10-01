import { omit } from 'lodash';
import { Client } from '../types/api';

export const clientDataWithoutSensitiveData = (clientData: Client) => {
  return omit(clientData, [
    'userId',
    'clientId',
    'clientSecret',
    'clientIdIssuedAt',
    'clientSecretExpiresAt',
  ]);
};
