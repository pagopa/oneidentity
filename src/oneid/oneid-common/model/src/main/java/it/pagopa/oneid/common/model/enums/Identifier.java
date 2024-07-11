package it.pagopa.oneid.common.model.enums;

public enum Identifier {
  spidCode("xs:string", "Codice SPID"),
  name("xs:string", "Nome"),
  familyName("xs:string", "Cognome"),
  placeOfBirth("xs:string", "Luogo di nascita"),
  countyOfBirth("xs:string", "Provincia di nascita"),
  dateOfBirth("xs:date", "Data di nascita"),
  gender("xs:string", "Sesso"),
  companyName("xs:string", "Nome azienda"),
  registeredOffice("xs:string", "Sede legale"),
  fiscalNumber("xs:string", "Codice fiscale"),
  ivaCode("xs:string", "Codice IVA"),
  idCard("xs:string", "Carta di identità"),
  mobilePhone("xs:string", "Telefono cellulare"),
  email("xs:string", "Email"),
  address("xs:string", "Indirizzo"),
  expirationDate("xs:date", "Data di scadenza"),
  digitalAddress("xs:string", "Indirizzo digitale"),
  domicileAddress("xs:string", "Indirizzo di domicilio"),
  domicilePlace("xs:string", "Luogo di domicilio"),
  domicilePostalCode("xs:string", "CAP di domicilio"),
  domicileProvince("xs:string", "Provincia di domicilio"),
  domicileCountry("xs:string", "Paese di domicilio"),
  qualification("xs:string", "Qualifica"),
  commonName("xs:string", "Nome comune"),
  surname("xs:string", "Cognome"),
  givenName("xs:string", "Nome di battesimo"),
  preferredUsername("xs:string", "Nome utente preferito"),
  title("xs:string", "Titolo"),
  userCertificate("xs:base64Binary", "Certificato utente"),
  employeeNumber("xs:string", "Numero dipendente"),
  orgUnitName("xs:string", "Nome unità organizzativa"),
  preferredLanguage("xs:string", "Lingua preferita"),
  country("xs:string", "Paese"),
  stateOrProvince("xs:string", "Stato o provincia"),
  city("xs:string", "Città"),
  postalCode("xs:string", "CAP"),
  street("xs:string", "Via");

  private final String type;
  private final String friendlyName;

  Identifier(String type, String friendlyName) {
    this.type = type;
    this.friendlyName = friendlyName;
  }

  public String getType() {
    return type;
  }

  public String getFriendlyName() {
    return friendlyName;
  }

  @Override
  public String toString() {
    return name() + " (" + type + ", " + friendlyName + ")";
  }
}
