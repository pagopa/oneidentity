const ERROR_MESSAGE = 'Es ist leider ein Fehler aufgetreten. ';
const PP_LINK = 'Datenschutzerklärung ';
const TOS_LINK = 'Allgemeine Geschäftsbedingungen ';

export default {
  common: {
    errorBoundary: {
      sessionModalTitle: 'Fehler',
      sessionModalMessage: ERROR_MESSAGE,
      toastError: 'FEHLER',
      toastMessage: ERROR_MESSAGE,
    },
    blockingErrorPage: {
      title: ERROR_MESSAGE,
      description:
        'Aufgrund eines Systemfehlers kann der Vorgang nicht abgeschlossen werden.',
      buttonLabel: 'Kontaktiere den Support',
    },
    footer: {
      legalInfoText:
        '<0>PagoPA S.p.A.</0> - Aktiengesellschaft mit Alleingesellschafter - Gesellschaftskapital von 1.000.000 Euro voll eingezahlt - Sitz in Rom, Piazza Colonna 370, <2/> PLZ 00187 - Eintrag im Handelsregister von Rom Nr., Steuernummer und USt-IdNr. 15376371009',
      privacyPolicyLink: PP_LINK,
      termsAndConditionLink:
        'Allgemeine Geschäftsbedingungen zur Benutzung der Site ',
      informationSecurityLink: 'Informationssicherheit ',
      assistanceLink: 'Betreuung ',
      preLoginLinks: {
        aboutUs: {
          links: {
            aboutUs: 'PagoPA S.p.A.',
            media: 'Medien',
            workwithud: 'Karriere',
          },
        },
        resources: {
          title: 'Quellen',
          links: {
            privacyPolicy: PP_LINK,
            certifications: 'Zertifizierungen',
            informationsecurity: 'Informationssicherheit',
            protectionofpersonaldata:
              'Recht auf Schutz personenbezogener Daten',
            cookies: 'Cookie-Einstellungen',
            termsandconditions: TOS_LINK,
            transparentcompany: 'Transparente Gesellschaft',
            disclosurePolicy: 'Responsible Disclosure Policy',
            model231: 'Modell 231',
          },
        },
        followUs: {
          title: 'Folg uns auf',
        },
        accessibility: 'Zugänglichkeit',
      },
      postLoginLinks: {
        privacyPolicy: PP_LINK,
        protectionofpersonaldata: 'Recht auf Schutz personenbezogener Daten',
        termsandconditions: TOS_LINK,
        accessibility: 'Zugänglichkeit',
      },
    },
    header: {
      exitButton: 'Abmelden',
    },
  },
  loginPage: {
    title: 'Zugriff auf die Area Riservata',
    description:
      'Der Bereich für die Körperschaften, die die Produkte von PagoPA nutzen.',
    temporaryLogin: {
      alert:
        'Wenn du dich mit SPID anmeldest und ein Problem auftritt, gehe zurück zu dieser Seite und drücke hier.',
      join: 'Hier eintreten',
    },
    loginBox: {
      spidLogin: 'Zugriff mit SPID',
      cieLogin: 'Zugriff mit CIE',
    },
    privacyAndCondition: {
      text: 'Wenn du dich anmeldest, akzeptierst du die {{termsLink}} des Dienstes und bestätigst, die {{privacyLink}} gelesen zu haben.',
      terms: TOS_LINK,
      privacy: 'Datenschutzerklärung',
    },
  },
  spidSelect: {
    title: 'Wähle deinen SPID-Anbieter',
    placeholder: 'Kein IDP gefunden',
    modalTitle: 'Wähle deinen Identitätsanbieter',
    cancelButton: 'Abbrechen',
    closeButton: 'Abmelden',
  },
  loginError: {
    retry: 'Erneut versuchen',
    close: 'Schließen',
    tooManyAttempts: {
      title: 'Du hast zu viele Anmeldeversuche gemacht',
      description:
        'Du hast zu oft einen falschen Benutzernamen oder ein falsches Passwort eingegeben. Bitte überprüfe deine Anmeldedaten und versuche es in ein paar Minuten erneut oder kontaktiere deinen SPID-Identitätsanbieter, um deine Zugangsdaten zu ändern.',
    },
    incompatibleCredentials: {
      title: 'Anmeldung nicht möglich',
      description:
        'Aus Sicherheitsgründen musst du eine Identität mit einem höheren Sicherheitsniveau verwenden. Für weitere Informationen wende dich bitte an deinen SPID-Identitätsanbieter.',
    },
    authTimeout: {
      title: 'Es ist zu viel Zeit vergangen',
      description:
        'Es ist zu viel Zeit vergangen, seit du mit der Anmeldung begonnen hast: Bitte starte von vorne.',
    },
    deniedByUser: {
      title: 'Du hast der Übermittlung von Daten nicht zugestimmt',
      description:
        'Um Zugang zu erhalten, musst du der Übermittlung bestimmter Daten zustimmen.',
    },
    suspendedOrRevoked: {
      title: 'Identität gesperrt oder widerrufen',
      description:
        'Deine SPID-Identität scheint gesperrt oder widerrufen zu sein. Für weitere Informationen wende dich bitte an deinen SPID-Identitätsanbieter.',
    },
    canceledByUser: {
      title: 'Du hast die Anmeldung abgebrochen',
      description: 'Um einzutreten, versuche es jederzeit erneut.',
    },
    generic: {
      title: 'Anmeldung nicht möglich',
      description:
        'Bei der Anmeldung ist ein Problem aufgetreten. Bitte versuche es in ein paar Minuten erneut.',
    },
  },
};
