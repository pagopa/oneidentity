export default {
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
