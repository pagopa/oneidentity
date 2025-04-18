export type LoginResponse = {
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

export type Client = {
  client_id: string;
  client_secret?: string;
  client_id_issued_at?: number;
  client_secret_expires_at?: number;
  client_name: string;
  logoUri: string;
  policy_uri: string;
  tos_uri: string;
  redirect_uris: Array<string>;
  saml_requested_attributes: Array<SamlAttribute>;
  logo_uri?: string;
  default_acr_values: Array<SpidLevel>;
};

export type ClientFormData = Omit<
  Client,
  | 'client_id'
  | 'client_secret'
  | 'client_id_issued_at'
  | 'client_secret_expires_at'
>;

export type RegisterClientRequest = {
  // Fields that can be submitted in the form
} & Omit<Client, 'client_id' | 'client_secret'>;

export type LoginError = {
  message: string;
  status: number;
};

export type FormData = {
  // Add any additional fields needed for the form
} & LoginResponse;
