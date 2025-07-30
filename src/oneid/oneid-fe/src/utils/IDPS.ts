export type IdentityProvider = {
  active: boolean;
  entityID: string;
  friendlyName: string;
  imageUrl: string;
};

export type IdentityProviders = {
  identityProviders: Array<IdentityProvider>;
  richiediSpid: string;
};
