import { z } from 'zod';

export type LoginResponse = {
  valid: boolean;
  client_id?: string;
};

export enum SpidLevel {
  // L1 = 'https://www.spid.gov.it/SpidL1',
  L2 = 'https://www.spid.gov.it/SpidL2',
  L3 = 'https://www.spid.gov.it/SpidL3',
}

export enum SamlAttribute {
  SPID_CODE = 'spidCode',
  NAME = 'name',
  FAMILY_NAME = 'familyName',
  FISCAL_NUMBER = 'fiscalNumber',
  DATE_OF_BIRTH = 'dateOfBirth',
  PLACE_OF_BIRTH = 'placeOfBirth',
  COUNTRY_OF_BIRTH = 'countyOfBirth',
  GENDER = 'gender',
  COMPANY_NAME = 'companyName',
  REGISTERED_OFFICE = 'registeredOffice',
  VAT_NUMBER = 'ivaCode',
  ID_CARD = 'idCard',
  MOBILE_PHONE = 'mobilePhone',
  EMAIL = 'email',
  DOMICILE_ADDRESS = 'domicileStreetAddress',
  DOMICILE_CAP = 'domicilePostalCode',
  DOMICILE_MUNICIPALITY = 'domicileMunicipality',
  DOMICILE_PROVINCE = 'domicileProvince',
  DOMICILE_NATION = 'domicileNation',
  DOMICILE_DIGITAL = 'digitalAddress',
  EXPIRATION_DATE = 'expirationDate',
}

export const SpidLevelSchema = z.enum([SpidLevel.L2, SpidLevel.L3]);
export const SamlAttributeSchema = z.enum([
  SamlAttribute.SPID_CODE,
  SamlAttribute.NAME,
  SamlAttribute.FAMILY_NAME,
  SamlAttribute.FISCAL_NUMBER,
  SamlAttribute.DATE_OF_BIRTH,
  SamlAttribute.PLACE_OF_BIRTH,
  SamlAttribute.COUNTRY_OF_BIRTH,
  SamlAttribute.GENDER,
  SamlAttribute.COMPANY_NAME,
  SamlAttribute.REGISTERED_OFFICE,
  SamlAttribute.VAT_NUMBER,
  SamlAttribute.ID_CARD,
  SamlAttribute.MOBILE_PHONE,
  SamlAttribute.EMAIL,
  SamlAttribute.DOMICILE_ADDRESS,
  SamlAttribute.DOMICILE_CAP,
  SamlAttribute.DOMICILE_MUNICIPALITY,
  SamlAttribute.DOMICILE_PROVINCE,
  SamlAttribute.DOMICILE_NATION,
  SamlAttribute.DOMICILE_DIGITAL,
  SamlAttribute.EXPIRATION_DATE,
]);
export const SamlAttributeArraySchema = z.array(SamlAttributeSchema);
export const SpidLevelArraySchema = z.array(SpidLevelSchema);

export const clientSchema = z.object({
  client_id: z.string().optional(),
  client_secret: z.string().optional(),
  client_id_issued_at: z.number().optional(),
  client_secret_expires_at: z.number().optional(),
  client_name: z.string(),
  policy_uri: z.string().url().optional().nullable(),
  tos_uri: z.string().url().optional().nullable(),
  redirect_uris: z.array(z.string().url().min(1)),
  saml_requested_attributes: SamlAttributeArraySchema.min(1),
  logo_uri: z.string().url().optional().nullable(),
  default_acr_values: SpidLevelArraySchema.min(1),
});

const LanguagesSchema = z.enum(['it', 'en', 'de', 'fr', 'sl']);

const ThemeSchema = z.object({
  title: z
    .string()
    .min(10, 'Title is required and must be at least 10 characters'),
  desc: z
    .string()
    .min(20, 'Description is required and must be at least 20 characters'),
  docUri: z.string().optional(),
  cookieUri: z.string().optional(),
  supportAddress: z.string().optional(),
});

const ThemeLocalizedSchema = z.record(LanguagesSchema, ThemeSchema);

export const clientFESchema = z.object({
  a11yUri: z.string().url().optional().nullable(),
  backButtonEnabled: z.boolean().optional().default(false),
  localizedContentMap: z.record(
    z.union([z.literal('default'), z.string()]),
    ThemeLocalizedSchema
  ),
});

export type Languages = z.infer<typeof LanguagesSchema>;
export type Client = z.infer<typeof clientSchema>;
export type ClientFE = z.infer<typeof clientFESchema>;
export type ClientThemeEntry = z.infer<typeof ThemeSchema>;
export type ClientLocalizedEntry = z.infer<typeof ThemeLocalizedSchema>;
export type ClientErrors = z.inferFormattedError<typeof clientSchema>;
export type ClientFEErrors = z.inferFormattedError<typeof clientFESchema>;

export type ClientFormData = Omit<
  Client,
  | 'client_id'
  | 'client_secret'
  | 'client_id_issued_at'
  | 'client_secret_expires_at'
>;

export type ClientRegisteredData = Pick<
  Client,
  | 'client_id'
  | 'client_secret'
  | 'client_id_issued_at'
  | 'client_secret_expires_at'
>;

export const allLanguages: Record<Languages, string> = {
  de: 'Deutsch',
  it: 'Italiano',
  en: 'English',
  sl: 'Slovenščina',
  fr: 'Français',
};

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
