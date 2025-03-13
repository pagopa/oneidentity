const ERROR_MESSAGE = 'Sorry, something went wrong. ';
const PP_LINK = 'Privacy Policy ';
const TOS_LINK = 'Terms and Conditions';

export default {
  common: {
    errorBoundary: {
      sessionModalTitle: 'Error',
      sessionModalMessage: ERROR_MESSAGE,
      toastError: 'ERROR',
      toastMessage: ERROR_MESSAGE,
    },
    blockingErrorPage: {
      title: ERROR_MESSAGE,
      description: 'Due to a system error, the procedure cannot be completed.',
      buttonLabel: 'Contact support',
    },
    footer: {
      legalInfoText:
        '<0>PagoPA S.p.A.</0> - Joint-stock company with sole shareholder - Share capital of €1,000,000 fully paid up - Registered office in Rome, Piazza Colonna 370, <2/> Postcode 00187 - Registration number in the Companies Register of Rome, Tax Code and VAT number 15376371009',
      privacyPolicyLink: PP_LINK,
      termsAndConditionLink: 'Website Terms and Conditions of Use ',
      informationSecurityLink: 'Information security ',
      assistanceLink: 'Support ',
      preLoginLinks: {
        aboutUs: {
          links: {
            aboutUs: 'PagoPA S.p.A.',
            media: 'Media',
            workwithud: 'Work with us',
          },
        },
        resources: {
          title: 'Resources',
          links: {
            privacyPolicy: PP_LINK,
            certifications: 'Certifications',
            informationsecurity: 'Information security',
            protectionofpersonaldata: 'Right to protection of personal data',
            cookies: 'Cookie Preferences',
            termsandconditions: TOS_LINK,
            transparentcompany: 'Transparent company',
            disclosurePolicy: 'Responsible Disclosure Policy',
            model231: 'Model 231',
          },
        },
        followUs: {
          title: 'Follow us on',
        },
        accessibility: 'Accessibility',
      },
      postLoginLinks: {
        privacyPolicy: PP_LINK,
        protectionofpersonaldata: 'Right to protection of personal data',
        termsandconditions: TOS_LINK,
        accessibility: 'Accessibility',
      },
    },
    header: {
      exitButton: 'Exit',
    },
  },
  loginPage: {
    title: 'Log in to the Area Riservata',
    description:
      'The area dedicated to organizations that use PagoPA products.',
    temporaryLogin: {
      alert:
        'If you log in with SPID and encounter a problem, return to this page and press here',
      join: 'Enter from here',
    },
    loginBox: {
      spidLogin: 'Log in with SPID',
      cieLogin: 'Log in with CIE',
    },
    privacyAndCondition: {
      text: 'By logging in, you accept the {{termsLink}} of the service and confirm that you have read the {{privacyLink}}.',
      terms: 'Terms and Conditions of Use',
      privacy: 'Privacy Policy',
    },
  },
  spidSelect: {
    title: 'Choose your SPID',
    placeholder: 'No IDP found',
    modalTitle: 'Choose your Identity Provider',
    cancelButton: 'Cancel',
    closeButton: 'Exit',
  },
  loginError: {
    retry: 'Retry',
    close: 'Close',
    tooManyAttempts: {
      title: 'You have made too many login attempts',
      description:
        'You have entered a username or password incorrectly too many times. Please check your login details and try again in a few minutes, or contact your SPID identity provider to change your credentials.',
    },
    incompatibleCredentials: {
      title: 'Unable to log in',
      description:
        'For security reasons, you must use an identity with a higher security level. For more information, contact your SPID identity provider.',
    },
    authTimeout: {
      title: 'Too much time has passed',
      description:
        'Too much time has passed since you started logging in: please restart from the beginning.',
    },
    deniedByUser: {
      title: 'You did not consent to the data submission',
      description:
        'To access, you need to consent to the submission of certain data.',
    },
    suspendedOrRevoked: {
      title: 'Identity suspended or revoked',
      description:
        'Your SPID identity appears to be suspended or revoked. For more information, contact your SPID identity provider.',
    },
    canceledByUser: {
      title: 'You canceled the login',
      description: 'To enter, please try again whenever you want.',
    },
    generic: {
      title: 'Unable to log in',
      description:
        'An error occurred during login. Please try again in a few minutes.',
    },
  },
};
