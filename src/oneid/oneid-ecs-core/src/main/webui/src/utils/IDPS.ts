export type IdentityProvider = {
  identifier: string;
  entityID: string;
  name: string;
  imageUrl: string;
};

export type IdentityProviders = {
  identityProviders: Array<IdentityProvider>;
  richiediSpid: string;
};
