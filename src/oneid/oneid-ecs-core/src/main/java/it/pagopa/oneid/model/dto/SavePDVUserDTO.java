package it.pagopa.oneid.model.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SavePDVUserDTO {

  private static final String FISCAL_CODE_PREFIX = "TINIT-";


  @NotNull
  String fiscalCode;

  String name;

  String familyName;

  String email;

  LocalDate birthDate;

  String spidCode;

  String placeOfBirth;

  String countyOfBirth;

  String gender;

  String companyName;

  String registeredOffice;

  String ivaCode;

  String idCard;

  String mobilePhone;

  String address;

  LocalDate expirationDate;

  String digitalAddress;

  String domicileAddress;

  String domicilePlace;

  String domicilePostalCode;

  String domicileProvince;

  String domicileCountry;

  String qualification;

  String commonName;

  String surname;

  String givenName;

  String preferredUsername;

  String title;

  String userCertificate;

  String employeeNumber;

  String orgUnitName;

  String preferredLanguage;

  String country;

  String stateOrProvince;

  String city;

  String postalCode;

  String street;

  public static SavePDVUserDTO fromAttributeDtoList(List<AttributeDTO> attributeDTOList) {
    SavePDVUserDTO.SavePDVUserDTOBuilder builder = SavePDVUserDTO.builder();

    for (AttributeDTO attr : attributeDTOList) {
      switch (attr.getAttributeName()) {
        case "fiscalCode":
          builder.fiscalCode(attr.getAttributeValue().replace(FISCAL_CODE_PREFIX, ""));
          break;
        case "name":
          builder.name(attr.getAttributeValue());
          break;
        case "familyName":
          builder.familyName(attr.getAttributeValue());
          break;
        case "email":
          builder.email(attr.getAttributeValue());
          break;
        case "birthDate":
          builder.birthDate(LocalDate.parse(attr.getAttributeValue()));
          break;
        case "spidCode":
          builder.spidCode(attr.getAttributeValue());
          break;
        case "placeOfBirth":
          builder.placeOfBirth(attr.getAttributeValue());
          break;
        case "countyOfBirth":
          builder.countyOfBirth(attr.getAttributeValue());
          break;
        case "gender":
          builder.gender(attr.getAttributeValue());
          break;
        case "companyName":
          builder.companyName(attr.getAttributeValue());
          break;
        case "registeredOffice":
          builder.registeredOffice(attr.getAttributeValue());
          break;
        case "ivaCode":
          builder.ivaCode(attr.getAttributeValue());
          break;
        case "idCard":
          builder.idCard(attr.getAttributeValue());
          break;
        case "mobilePhone":
          builder.mobilePhone(attr.getAttributeValue());
          break;
        case "address":
          builder.address(attr.getAttributeValue());
          break;
        case "expirationDate":
          builder.expirationDate(LocalDate.parse(attr.getAttributeValue()));
          break;
        case "digitalAddress":
          builder.digitalAddress(attr.getAttributeValue());
          break;
        case "domicileAddress":
          builder.domicileAddress(attr.getAttributeValue());
          break;
        case "domicilePlace":
          builder.domicilePlace(attr.getAttributeValue());
          break;
        case "domicilePostalCode":
          builder.domicilePostalCode(attr.getAttributeValue());
          break;
        case "domicileProvince":
          builder.domicileProvince(attr.getAttributeValue());
          break;
        case "domicileCountry":
          builder.domicileCountry(attr.getAttributeValue());
          break;
        case "qualification":
          builder.qualification(attr.getAttributeValue());
          break;
        case "commonName":
          builder.commonName(attr.getAttributeValue());
          break;
        case "surname":
          builder.surname(attr.getAttributeValue());
          break;
        case "givenName":
          builder.givenName(attr.getAttributeValue());
          break;
        case "preferredUsername":
          builder.preferredUsername(attr.getAttributeValue());
          break;
        case "title":
          builder.title(attr.getAttributeValue());
          break;
        case "userCertificate":
          builder.userCertificate(attr.getAttributeValue());
          break;
        case "employeeNumber":
          builder.employeeNumber(attr.getAttributeValue());
          break;
        case "orgUnitName":
          builder.orgUnitName(attr.getAttributeValue());
          break;
        case "preferredLanguage":
          builder.preferredLanguage(attr.getAttributeValue());
          break;
        case "country":
          builder.country(attr.getAttributeValue());
          break;
        case "stateOrProvince":
          builder.stateOrProvince(attr.getAttributeValue());
          break;
        case "city":
          builder.city(attr.getAttributeValue());
          break;
        case "postalCode":
          builder.postalCode(attr.getAttributeValue());
          break;
        case "street":
          builder.street(attr.getAttributeValue());
          break;
        
      }
    }

    return builder.build();
  }

}