const ERROR_MESSAGE = 'Žal, nekaj je šlo narobe.';

export default {
  common: {
    errorBoundary: {
      sessionModalTitle: 'Napaka',
      sessionModalMessage: ERROR_MESSAGE,
      toastError: 'NAPAKA',
      toastMessage: ERROR_MESSAGE,
    },
    blockingErrorPage: {
      title: ERROR_MESSAGE,
      description: 'Zaradi sistemske napake postopka ni mogoče dokončati.',
      buttonLabel: 'Stopite v stik s podporo',
    },
    footer: {
      legalInfoText:
        '<0>PagoPA S.p.A.</0> - Delniška družba z enim družbenikom - Osnovni kapital v višini 1.000.000 EUR v celoti vplačan - Sedež v Rimu, Piazza Colonna 370, <2/> Poštna številka 00187 - Št. vpisa v poslovni register v Rimu, davčna številka in identifikacijska številka za DDV 15376371009',
      privacyPolicyLink: 'Politika zasebnosti ',
      termsAndConditionLink: 'Pogoji uporabe spletnega mesta ',
      informationSecurityLink: 'Varnost podatkov ',
      assistanceLink: 'Podpora strankam ',
      preLoginLinks: {
        aboutUs: {
          links: {
            aboutUs: 'PagoPA S.p.A.',
            media: 'Mediji',
            workwithud: 'Sodeluj z nami',
          },
        },
        resources: {
          title: 'Viri',
          links: {
            privacyPolicy: 'Politika zasebnosti',
            certifications: 'Certifikati',
            informationsecurity: 'Varnost podatkov',
            protectionofpersonaldata: 'Pravica do varstva osebnih podatkov',
            cookies: 'Nastavitve piškotkov',
            termsandconditions: 'Pogoji in določila',
            transparentcompany: 'Pregledna družba',
            disclosurePolicy: 'Politika odgovornega razkritja',
            model231: 'Model 231',
          },
        },
        followUs: {
          title: 'Spremljajte nas na',
        },
        accessibility: 'Dostopnost',
      },
      postLoginLinks: {
        privacyPolicy: 'Pravilnik o zasebnosti',
        protectionofpersonaldata: 'Pravica do varstva osebnih podatkov',
        termsandconditions: 'Pogoji in določila',
        accessibility: 'Dostopnost',
      },
    },
    header: {
      exitButton: 'Izhod',
    },
  },
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
    placeholder: 'Ni najdenega IDP-ja',
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
