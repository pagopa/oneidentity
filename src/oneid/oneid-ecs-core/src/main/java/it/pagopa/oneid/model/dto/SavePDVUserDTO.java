package it.pagopa.oneid.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class SavePDVUserDTO {

  private static final String FISCAL_CODE_PREFIX = "TINIT-";

  @NotNull
  private String fiscalCode;

  private CertifiedField<String> name;

  private CertifiedField<String> familyName;

  private CertifiedField<String> email;

  private CertifiedField<LocalDate> birthDate;

  private CertifiedField<String> spidCode;

  private CertifiedField<String> placeOfBirth;

  private CertifiedField<String> countyOfBirth;

  private CertifiedField<String> gender;

  private CertifiedField<String> companyName;

  private CertifiedField<String> registeredOffice;

  private CertifiedField<String> ivaCode;

  private CertifiedField<String> idCard;

  private CertifiedField<String> mobilePhone;

  private CertifiedField<String> address;

  private CertifiedField<LocalDate> expirationDate;

  private CertifiedField<String> digitalAddress;

  private CertifiedField<String> domicileAddress;

  private CertifiedField<String> domicilePlace;

  private CertifiedField<String> domicilePostalCode;

  private CertifiedField<String> domicileProvince;

  private CertifiedField<String> domicileCountry;

  private CertifiedField<String> qualification;

  private CertifiedField<String> commonName;

  private CertifiedField<String> surname;

  private CertifiedField<String> givenName;

  private CertifiedField<String> preferredUsername;

  private CertifiedField<String> title;

  private CertifiedField<String> userCertificate;

  private CertifiedField<String> employeeNumber;

  private CertifiedField<String> orgUnitName;

  private CertifiedField<String> preferredLanguage;

  private CertifiedField<String> country;

  private CertifiedField<String> stateOrProvince;

  private CertifiedField<String> city;

  private CertifiedField<String> postalCode;

  private CertifiedField<String> street;

  public static SavePDVUserDTO fromAttributeDtoList(List<AttributeDTO> attributeDTOList) {
    SavePDVUserDTO.SavePDVUserDTOBuilder builder = SavePDVUserDTO.builder();

    for (AttributeDTO attr : attributeDTOList) {
      if (!StringUtils.isBlank(attr.getAttributeValue())) {
        switch (attr.getAttributeName()) {
          case "fiscalNumber":
            builder.fiscalCode(attr.getAttributeValue().replace(FISCAL_CODE_PREFIX, ""));
            break;
          case "name":
            builder.name(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "familyName":
            builder.familyName(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "email":
            builder.email(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "birthDate":
            builder.birthDate(new CertifiedField<>(LocalDate.parse(attr.getAttributeValue())));
            break;
          case "spidCode":
            builder.spidCode(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "placeOfBirth":
            builder.placeOfBirth(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "countyOfBirth":
            builder.countyOfBirth(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "gender":
            builder.gender(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "companyName":
            builder.companyName(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "registeredOffice":
            builder.registeredOffice(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "ivaCode":
            builder.ivaCode(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "idCard":
            builder.idCard(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "mobilePhone":
            builder.mobilePhone(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "address":
            builder.address(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "expirationDate":
            builder.expirationDate(new CertifiedField<>(LocalDate.parse(attr.getAttributeValue())));
            break;
          case "digitalAddress":
            builder.digitalAddress(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "domicileAddress":
            builder.domicileAddress(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "domicilePlace":
            builder.domicilePlace(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "domicilePostalCode":
            builder.domicilePostalCode(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "domicileProvince":
            builder.domicileProvince(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "domicileCountry":
            builder.domicileCountry(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "qualification":
            builder.qualification(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "commonName":
            builder.commonName(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "surname":
            builder.surname(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "givenName":
            builder.givenName(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "preferredUsername":
            builder.preferredUsername(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "title":
            builder.title(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "userCertificate":
            builder.userCertificate(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "employeeNumber":
            builder.employeeNumber(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "orgUnitName":
            builder.orgUnitName(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "preferredLanguage":
            builder.preferredLanguage(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "country":
            builder.country(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "stateOrProvince":
            builder.stateOrProvince(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "city":
            builder.city(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "postalCode":
            builder.postalCode(new CertifiedField<>(attr.getAttributeValue()));
            break;
          case "street":
            builder.street(new CertifiedField<>(attr.getAttributeValue()));
            break;

        }
      }
    }

    return builder.build();
  }

}