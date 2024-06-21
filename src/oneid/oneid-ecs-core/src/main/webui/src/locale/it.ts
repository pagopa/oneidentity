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
      title: 'Login',
      spidLogin: 'Entra con SPID',
      cieLogin: 'Entra con CIE',
    },
    privacyAndCondition:
      "Accedendo accetti i <1>Termini e condizioni d’uso</1> del servizio e <3 /> confermi di avere letto <5>l'Informativa Privacy</5>",
  },
  loginPageFromOnboarding: {
    title: 'Come vuoi accedere?',
    description:
      'Seleziona la modalità di accesso che preferisci e inizia il <1 /> processo di  adesione al prodotto <3>{{nomeProdotto}}<3/>.',
    ptDescription:
      'Seleziona la modalità che preferisci per accedere e registrarti <1 /> come Partner tecnologico per il prodotto <3>{{nomeProdotto}}<3/>.',
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
      title: 'Hai effettuato troppi tentativi di <1 />accesso',
      description: 'Hai inserito troppe volte un nome utente o password non corretti. <1 />Verifica i dati di accesso e riprova fra qualche minuto, o contatta il <3 />tuo fornitore di identità SPID per modificare le tue credenziali.'
    },
    incompatibleCredentials: {
      title: 'Non è stato possibile accedere',
      description: 'Per motivi di sicurezza, devi utilizzare un’identità con un livello di <1 />sicurezza superiore. Per avere più informazioni, contatta il tuo <3 />fornitore di identità SPID.'
    },
    authTimeout: {
      title: 'È passato troppo tempo',
      description: "È passato troppo tempo da quando hai iniziato l'accesso: riparti <1 />dall'inizio."
    },
    deniedByUser: {
      title: 'Non hai dato il consenso all’invio <1 />dei dati',
      description: 'Per accedere, è necessario acconsentire all’invio di alcuni dati.'
    },
    suspendedOrRevoked: {
      title: 'Identità sospesa o revocata',
      message: 'La tua identità SPID risulta sospesa o revocata. Per maggiori <1/>informazioni, contatta il tuo fornitore di identità SPID.',
    },
    canceledbyUser: {
      title: 'Hai annullato l’accesso',
      description: 'Per entrare, riprova quando vuoi.'
    },
    generic: {
      title: 'Non è stato possibile accedere',
      description: 'Si è verificato un problema durante l’accesso. Riprova tra qualche <1/>minuto.'
    },
  },
};
