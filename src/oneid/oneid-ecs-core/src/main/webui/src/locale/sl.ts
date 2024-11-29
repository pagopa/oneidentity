export default {
  loginPage: {
    title: 'Dostop do rezerviranega območja',
    description: 'Prostor, namenjen subjektom, ki uporabljajo izdelke PagoPA.',
    temporaryLogin: {
      alert:
        'Če se prijavljate s SPID in naletite na težavo, se vrnite na to stran in pritisnite tukaj.',
      join: 'Vstopite tukaj',
    },
    loginBox: {
      spidLogin: 'Dostopaj s SPID',
      cieLogin: 'Dostopaj s CIE',
    },
    privacyAndCondition: {
      text: 'Z dostopom se strinjate s {{termsLink}} storitve in potrjujete, da ste prebrali {{privacyLink}}.',
      terms: 'Pogoji in določila uporabe',
      privacy: 'Izjava o zasebnosti',
    },
  },
  spidSelect: {
    title: 'Izberite svoj SPID',
    modalTitle: 'Izberite svoj ponudnik identitete',
    cancelButton: 'Prekliči',
    closeButton: 'Izhod',
  },
  loginError: {
    retry: 'Poskusi znova',
    close: 'Zapri',
    tooManyAttempts: {
      title: 'Preveč poskusov prijave',
      description:
        'Prevečkrat ste vnesli napačno uporabniško ime ali geslo. Preverite svoje prijavne podatke in poskusite znova čez nekaj minut ali se obrnite na svojega ponudnika identitete SPID za spremembo svojih poverilnic.',
    },
    incompatibleCredentials: {
      title: 'Prijava ni mogoča',
      description:
        'Iz varnostnih razlogov morate uporabiti identiteto z višjo stopnjo varnosti. Za več informacij se obrnite na svojega ponudnika identitete SPID.',
    },
    authTimeout: {
      title: 'Preveč časa je minilo',
      description: 'Minilo je preveč časa od začetka prijave: začnite znova.',
    },
    deniedByUser: {
      title: 'Niste dali soglasja za posredovanje podatkov',
      description:
        'Za dostop se morate strinjati s posredovanjem nekaterih podatkov.',
    },
    suspendedOrRevoked: {
      title: 'Identiteta je bila suspendirana ali preklicana',
      description:
        'Vaša identiteta SPID se zdi, da je bila suspendirana ali preklicana. Za več informacij se obrnite na svojega ponudnika identitete SPID.',
    },
    canceledByUser: {
      title: 'Prijavo ste preklicali',
      description: 'Za vstop poskusite znova kadarkoli.',
    },
    generic: {
      title: 'Prijava ni mogoča',
      description:
        'Pri prijavi je prišlo do težave. Poskusite znova čez nekaj minut.',
    },
  },
};
