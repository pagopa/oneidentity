export default {
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
      terms: 'Allgemeine Geschäftsbedingungen',
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
