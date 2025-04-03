import { trackEvent } from './analyticsService';

export const buildAssistanceURI = (assistanceString?: string) => {
  const expressionUri =
    /(https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|www\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\.[^\s]{2,}|https?:\/\/(?:www\.|(?!www))[a-zA-Z0-9]+\.[^\s]{2,}|www\.[a-zA-Z0-9]+\.[^\s]{2,})/gi;
  const regex = new RegExp(expressionUri);
  const expressionEmail = /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/gi;
  const regexEmail = new RegExp(expressionEmail);

  if (assistanceString && assistanceString.match(regex)) {
    trackEvent('CUSTOMER_CARE_LINK');
    return `${assistanceString}`;
  } else if (assistanceString && assistanceString.match(regexEmail)) {
    trackEvent('CUSTOMER_CARE_MAILTO');
    return `mailto:${assistanceString}`;
  } else {
    trackEvent('CUSTOMER_CARE_INVALID_LINK');
    return null;
  }
};
