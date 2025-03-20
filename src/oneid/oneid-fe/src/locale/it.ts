const ERROR_MESSAGE = 'Spiacenti, qualcosa è andato storto.';
const PP_LINK = 'Informativa Privacy';

export default {
  common: {
    errorBoundary: {
      sessionModalTitle: 'Errore',
      sessionModalMessage: ERROR_MESSAGE,
      toastError: 'ERRORE',
      toastMessage: ERROR_MESSAGE,
    },
    blockingErrorPage: {
      title: ERROR_MESSAGE,
      description:
        'A causa di un errore del sistema non è possibile completare la procedura.',
      buttonLabel: "Contatta l'assistenza",
    },
    footer: {
      legalInfoText: `<0>PagoPA S.p.A.</0> - Società per azioni con socio unico - Capitale sociale di euro 1,000,000 interamente versato - Sede legale in Roma, Piazza Colonna 370, <2/> CAP 00187 - N. di iscrizione a Registro Imprese di Roma, CF e P.IVA 15376371009`,
      privacyPolicyLink: PP_LINK,
      termsAndConditionLink: 'Termini e condizioni d’uso del sito ',
      informationSecurityLink: 'Sicurezza delle informazioni ',
      assistanceLink: 'Assistenza ',
      preLoginLinks: {
        aboutUs: {
          links: {
            aboutUs: 'PagoPA S.p.A.',
            media: 'Media',
            workwithud: 'Lavora con noi',
          },
        },
        resources: {
          title: 'Risorse',
          links: {
            privacyPolicy: PP_LINK,
            certifications: 'Certificazioni',
            informationsecurity: 'Sicurezza delle informazioni',
            protectionofpersonaldata:
              'Diritto alla protezione dei dati personali',
            cookies: 'Preferenze Cookie',
            termsandconditions: 'Termini e Condizioni',
            transparentcompany: 'Società trasparente',
            disclosurePolicy: 'Responsible Disclosure Policy',
            model231: 'Modello 231',
          },
        },
        followUs: {
          title: 'Seguici su',
        },
        accessibility: 'Accessibilità',
      },
      postLoginLinks: {
        privacyPolicy: PP_LINK,
        protectionofpersonaldata: 'Diritto alla protezione dei dati personali',
        termsandconditions: 'Termini e Condizioni',
        accessibility: 'Accessibilità',
      },
    },
    header: {
      exitButton: 'Esci',
    },
    backButtonText: 'Indietro',
  },
  loginPage: {
    title: 'Come vuoi entrare?',
    description: 'Scegli il metodo di autenticazione che preferisci',
    temporaryLogin: {
      alert:
        'Se entri con SPID e riscontri un problema, torna su questa pagina e premi qui accanto',
      join: 'Entra da qui',
    },
    loginBox: {
      spidLogin: 'Entra con SPID',
      cieLogin: 'Entra con CIE',
    },
    privacyAndCondition: {
      text: "Accedendo accetti i {{termsLink}} del servizio e confermi di avere letto l'{{privacyLink}}",
      terms: 'Termini e condizioni d’uso',
      privacy: 'Informativa Privacy',
    },
  },
  spidSelect: {
    title: 'Scegli il tuo SPID',
    placeholder: 'Nessun IDP trovato',
    modalTitle: 'Scegli il tuo Identity Provider',
    cancelButton: 'Annulla',
    closeButton: 'Esci',
  },
  loginError: {
    retry: 'Riprova',
    close: 'Chiudi',
    tooManyAttempts: {
      title: 'Hai effettuato troppi tentativi di accesso',
      description:
        'Hai inserito troppe volte un nome utente o password non corretti. Verifica i dati di accesso e riprova fra qualche minuto, o contatta il tuo fornitore di identità SPID per modificare le tue credenziali.',
    },
    incompatibleCredentials: {
      title: 'Non è stato possibile accedere',
      description:
        'Per motivi di sicurezza, devi utilizzare un’identità con un livello di sicurezza superiore. Per avere più informazioni, contatta il tuo fornitore di identità SPID.',
    },
    authTimeout: {
      title: 'È passato troppo tempo',
      description:
        "È passato troppo tempo da quando hai iniziato l'accesso: riparti dall'inizio.",
    },
    deniedByUser: {
      title: 'Non hai dato il consenso all’invio dei dati',
      description:
        'Per accedere, è necessario acconsentire all’invio di alcuni dati.',
    },
    suspendedOrRevoked: {
      title: 'Identità sospesa o revocata',
      description:
        'La tua identità SPID risulta sospesa o revocata. Per maggiori informazioni, contatta il tuo fornitore di identità SPID.',
    },
    canceledbyUser: {
      title: 'Hai annullato l’accesso',
      description: 'Per entrare, riprova quando vuoi.',
    },
    generic: {
      title: 'Non è stato possibile accedere',
      description:
        'Si è verificato un problema durante l’accesso. Riprova tra qualche minuto.',
    },
  },
};
