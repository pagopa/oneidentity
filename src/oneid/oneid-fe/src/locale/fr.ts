const ERROR_MESSAGE = 'Désolé, quelque chose s’est mal passé. ';

export default {
  common: {
    errorBoundary: {
      sessionModalTitle: 'Erreur',
      sessionModalMessage: ERROR_MESSAGE,
      toastError: 'ERREUR',
      toastMessage: ERROR_MESSAGE,
    },
    blockingErrorPage: {
      title: ERROR_MESSAGE,
      description:
        'En raison d’une erreur du système, il n’est pas possible de terminer la procédure.',
      buttonLabel: 'Contacter le support',
    },
    footer: {
      legalInfoText:
        '<0>PagoPA S.p.A.</0> - Société anonyme à associé unique - Capital social de 1,000,000 euros entièrement libéré - Siège social à Rome, Piazza Colonna 370, <2/> CP 00187 - N ° d’immatriculation au Registre du Commerce et des Sociétés de Rome, N ° de TVA 15376371009',
      privacyPolicyLink: 'politique de confidentialité ',
      termsAndConditionLink: 'Conditions générales d’utilisation ',
      informationSecurityLink: 'Sécurité des informations ',
      assistanceLink: 'Assistance ',
      preLoginLinks: {
        aboutUs: {
          links: {
            aboutUs: 'PagoPA S.p.A.',
            media: 'Médias',
            workwithud: 'Carrière',
          },
        },
        resources: {
          title: 'Ressources',
          links: {
            privacyPolicy: 'Politique de confidentialité',
            certifications: 'Certifications',
            informationsecurity: 'Sécurité des informations',
            protectionofpersonaldata:
              'Droit à la protection des données personnelles',
            cookies: 'Préférences en matière de cookies',
            termsandconditions: 'Conditions générales',
            transparentcompany: 'Société transparente',
            disclosurePolicy: 'Politique de divulgation responsable',
            model231: 'Modèle 231',
          },
        },
        followUs: {
          title: 'Suivez-nous sur',
        },
        accessibility: 'Accessibilité',
      },
      postLoginLinks: {
        privacyPolicy: 'Politique de confidentialité',
        protectionofpersonaldata:
          'Droit à la protection des données personnelles',
        termsandconditions: 'Conditions générales',
        accessibility: 'Accessibilité',
      },
    },
    header: {
      exitButton: 'Quitter',
    },
    backButtonText: 'Retour',
  },
  loginPage: {
    title: "Accéder à l'Espace réservé",
    description:
      "L'espace dédié aux organismes qui utilisent les produits de PagoPA.",
    temporaryLogin: {
      alert:
        'Si vous vous connectez avec SPID et rencontrez un problème, revenez sur cette page et appuyez ici.',
      join: 'Entrez par ici',
    },
    loginBox: {
      spidLogin: 'Se connecter avec SPID',
      cieLogin: 'Se connecter avec CIE',
    },
    privacyAndCondition: {
      text: 'En vous connectant, vous acceptez les {{termsLink}} du service et confirmez avoir lu la {{privacyLink}}.',
      terms: 'Conditions d’utilisation',
      privacy: 'Politique sur le respect de la vie privée',
    },
  },
  spidSelect: {
    title: 'Choisir votre SPID',
    placeholder: 'Aucun IDP trouvé',
    modalTitle: 'Choisissez votre fournisseur d’identité',
    cancelButton: 'Annuler',
    closeButton: 'Quitter',
  },
  loginError: {
    retry: 'Réessayer',
    close: 'Fermer',
    tooManyAttempts: {
      title: 'Vous avez effectué trop de tentatives de connexion',
      description:
        "Vous avez saisi un nom d'utilisateur ou un mot de passe incorrect trop de fois. Veuillez vérifier vos identifiants et réessayer dans quelques minutes, ou contactez votre fournisseur d’identité SPID pour modifier vos informations d'identification.",
    },
    incompatibleCredentials: {
      title: 'Impossible de se connecter',
      description:
        "Pour des raisons de sécurité, vous devez utiliser une identité ayant un niveau de sécurité supérieur. Pour plus d'informations, contactez votre fournisseur d’identité SPID.",
    },
    authTimeout: {
      title: "Trop de temps s'est écoulé",
      description:
        "Trop de temps s'est écoulé depuis le début de la connexion : veuillez recommencer depuis le début.",
    },
    deniedByUser: {
      title: "Vous n'avez pas donné votre consentement à l'envoi de données",
      description:
        "Pour accéder, vous devez consentir à l'envoi de certaines données.",
    },
    suspendedOrRevoked: {
      title: 'Identité suspendue ou révoquée',
      description:
        "Votre identité SPID semble être suspendue ou révoquée. Pour plus d'informations, contactez votre fournisseur d’identité SPID.",
    },
    canceledByUser: {
      title: 'Vous avez annulé la connexion',
      description: 'Pour entrer, réessayez quand vous le souhaitez.',
    },
    generic: {
      title: 'Impossible de se connecter',
      description:
        'Un problème est survenu lors de la connexion. Veuillez réessayer dans quelques minutes.',
    },
  },
};
