import { trackEvent } from './analyticsService';

export const buildAssistanceURI = (assistanceEmail?: string) => {
  trackEvent('CUSTOMER_CARE_MAILTO');
  return `mailto:${assistanceEmail}`;
};
