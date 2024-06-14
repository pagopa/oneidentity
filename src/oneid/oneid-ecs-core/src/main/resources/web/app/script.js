const searchParams = new URLSearchParams(window.location.search);
for (const param of searchParams) {
  console.log(param);
}
console.log(searchParams);
const params = encodeURIComponent(searchParams)
console.log(`/oidc/authorize?idp={{idp}}&${decodeURIComponent(params)}`);
SPID.init({
  url: `/oidc/authorize?idp={{idp}}&${decodeURIComponent(params)}`,
  method: 'POST',
  supported: ["https://demo.spid.gov.it"],
  extraProviders: [
    {
      protocols: ["SAML"],
      entityName: "IDP test",
      logo: "spid-idp-aruba.svg",
      entityID: "https://localhost:8443/samlsso",
      active: true,
    },
    {
      protocols: ["SAML"],
      entityName: "IDP demo",
      logo: "spid-idp-aruba.svg",
      entityID: "https://demo.spid.gov.it",
      active: true,
    },
    {
      protocols: ["SAML"],
      entityName: "IDP local demo",
      logo: "spid-idp-aruba.svg",
      entityID: "https://localhost:8443/demo/samlsso",
      active: true,
    },
  ],
});
