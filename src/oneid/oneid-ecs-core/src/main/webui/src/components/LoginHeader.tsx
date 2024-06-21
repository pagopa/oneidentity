import { useEffect, useState } from 'react';
import { ENV } from '../utils/env';
import i18n from '../locale';
import { trackEvent } from '../services/analyticsService';
import Header from './Header';

export const LoginHeader = () => {
  const [showDocBtn, setShowDocBtn] = useState(false);

  useEffect(() => {
    if (i18n.language === 'it') {
      setShowDocBtn(true);
    } else {
      setShowDocBtn(false);
    }
  }, [i18n.language]);

  return (
    <Header
      withSecondHeader={false}
      enableAssistanceButton={ENV.ENV !== 'UAT'}
      assistanceEmail={ENV.ASSISTANCE.ENABLE ? ENV.ASSISTANCE.EMAIL : undefined}
      enableLogin={false}
      loggedUser={false}
      onDocumentationClick={
        showDocBtn
          ? () => {
              trackEvent('OPEN_OPERATIVE_MANUAL', {
                from: 'login',
              });
              window.open(ENV.URL_DOCUMENTATION, '_blank');
            }
          : undefined
      }
    />
  );
};
