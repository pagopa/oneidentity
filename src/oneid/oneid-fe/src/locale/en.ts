export default {
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
