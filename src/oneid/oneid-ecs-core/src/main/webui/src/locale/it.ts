export default {
  loginPage: {
    title: 'Accedi all’Area Riservata',
    description: 'Lo spazio dedicato agli enti che utilizzano i prodotti di PagoPA.',
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
      description: 'Per accedere, è necessario acconsentire all’invio di alcuni dati.',
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
