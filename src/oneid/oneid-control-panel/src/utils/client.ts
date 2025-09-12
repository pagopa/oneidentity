import { omit } from 'lodash';
import { Client } from '../types/api';

export const clientDataWithoutSensitiveData = (clientData: Client) => {
  return omit(clientData, [
    'clientId',
    'clientSecret',
    'clientIdIssuedAt',
    'clientSecretExpiresAt',
  ]);
};
