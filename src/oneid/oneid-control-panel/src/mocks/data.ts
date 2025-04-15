import { ClientData, SpidLevel, SamlAttribute } from '../types/api';

export const mockClientData: ClientData = {
  client_id: 'aaaaaaaaaaaaaa-aaaaaaaaaaaaaaaaaaaaaaaa',
  client_secret: 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
  client_id_issued_at: 1740589972573,
  client_secret_expires_at: 0,
  client_name: 'test_2',
  redirect_uris: ['https://client.example.org/callback'],
  saml_requested_attributes: [SamlAttribute.FISCAL_NUMBER],
  logo_uri: 'http://test.com/logo.png',
  policy_uri: 'http://test.com/policy_uri.html',
  tos_uri: 'http://test.com/tos_uri.html',
  default_acr_values: [SpidLevel.L2],
};

export const VALID_API_KEY = 'test-api-key-123';
