import { Client, SamlAttribute, SpidLevel } from '../types/api';
import { clientDataWithoutSensitiveData } from './client';

describe('clientDataWithoutSensitiveData', () => {
  it('should remove sensitive data from client data object', () => {
    const clientData: Client = {
      userId: 'user-123',
      clientId: 'secret-client-id',
      clientSecret: 'super-secret',
      clientIdIssuedAt: 1234567890,
      clientSecretExpiresAt: 987654321,
      clientName: 'Test Client',
      policyUri: 'https://example.com/policy',
      tosUri: 'https://example.com/tos',
      redirectUris: ['https://example.com/callback'],
      samlRequestedAttributes: [SamlAttribute.FISCAL_NUMBER],
      logoUri: 'https://example.com/logo.png',
      defaultAcrValues: [SpidLevel.L2],
      requiredSameIdp: true,
      spidMinors: false,
      spidProfessionals: true,
      pairwise: false,
      a11yUri: 'https://example.com/accessibility',
      backButtonEnabled: true,
      localizedContentMap: {
        default: {
          it: {
            title: 'title',
            description: 'description',
          },
        },
        it: {
          it: {
            title: 'title',
            description: 'description',
          },
        },
      },
    };

    const result = clientDataWithoutSensitiveData(clientData);

    expect(result).not.toHaveProperty('clientId');
    expect(result).not.toHaveProperty('clientSecret');
    expect(result).not.toHaveProperty('clientIdIssuedAt');
    expect(result).not.toHaveProperty('clientSecretExpiresAt');

    expect(result).toMatchObject({
      userId: 'user-123',
      clientName: 'Test Client',
      policyUri: 'https://example.com/policy',
      tosUri: 'https://example.com/tos',
      redirectUris: ['https://example.com/callback'],
      samlRequestedAttributes: [SamlAttribute.FISCAL_NUMBER],
      logoUri: 'https://example.com/logo.png',
      defaultAcrValues: [SpidLevel.L2],
      requiredSameIdp: true,
      spidMinors: false,
      spidProfessionals: true,
      pairwise: false,
      a11yUri: 'https://example.com/accessibility',
      backButtonEnabled: true,
      localizedContentMap: {
        default: {
          it: {
            title: 'title',
            description: 'description',
          },
        },
        it: {
          it: {
            title: 'title',
            description: 'description',
          },
        },
      },
    });
  });

  it('should works even if sensitive data is missing', () => {
    const clientData: Partial<Client> = {
      clientName: 'Another Client',
      redirectUris: ['https://example.com/redirect'],
      samlRequestedAttributes: [SamlAttribute.EMAIL],
      defaultAcrValues: [SpidLevel.L2],
    };

    const result = clientDataWithoutSensitiveData(clientData as Client);

    expect(result).toHaveProperty('clientName', 'Another Client');
    expect(result).not.toHaveProperty('clientId');
    expect(result).not.toHaveProperty('clientSecret');
  });
});
