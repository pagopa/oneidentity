import { LangCode } from '@pagopa/mui-italia';
export interface LoginResponse {
  valid: boolean;
  client_id?: string;
}

export enum SpidLevel {
  L1 = 'https://www.spid.gov.it/SpidL1',
  L2 = 'https://www.spid.gov.it/SpidL2',
  L3 = 'https://www.spid.gov.it/SpidL3',
}

export enum SamlAttribute {
  SPID_CODE = 'spidCode',
  NAME = 'name',
  FAMILY_NAME = 'familyName',
  FISCAL_NUMBER = 'fiscalNumber',
}

export interface ClientData {
  client_id: string;
  client_secret?: string;
  client_id_issued_at?: number;
  client_secret_expires_at?: number;
  client_name: string;
  policy_uri?: string;
  tos_uri?: string;
  redirect_uris: string[];
  saml_requested_attributes: SamlAttribute[];
  logo_uri?: string;
  default_acr_values: SpidLevel[];
}

export type Client = {
  clientID: string;
  friendlyName: string;
  logoUri: string;
  policyUri: string;
  tosUri: string;
  docUri: string;
  a11yUri: string;
  cookieUri: string;
  callbackURI: Array<string>;
  supportAddress: string;
  backButtonEnabled: boolean;
  localizedContentMap: Record<
    LangCode,
    Record<'title' | 'description', string>
  >;
};

export type ClientFormData = Omit<
  ClientData,
  | 'client_id'
  | 'client_secret'
  | 'client_id_issued_at'
  | 'client_secret_expires_at'
>;

export interface RegisterClientRequest
  extends Omit<ClientData, 'client_id' | 'client_secret'> {
  // Fields that can be submitted in the form
}

export interface LoginError {
  message: string;
  status: number;
}

export interface FormData extends LoginResponse {
  // Add any additional fields needed for the form
}
