import { initReactI18next } from 'react-i18next';
import i18n from 'i18next';
import it from './it';
import en from './en';
import fr from './fr';
import de from './de';
import sl from './sl';

export const configureI18n = (
  resources: { [lang: string]: any },
  defaultLanguage: string = 'it'
) => {
  const completeResources = {
    ...resources,
    it: { translation: { ...it, ...resources.it } },
    en: { translation: { ...en, ...resources.en } },
    fr: { translation: { ...fr, ...resources.fr } },
    de: { translation: { ...de, ...resources.de } },
    sl: { translation: { ...sl, ...resources.sl } },
  };
  i18n
    .use(initReactI18next)
    .init({
      resources: completeResources,
      lng: defaultLanguage,
      interpolation: {
        escapeValue: false,
      },
    })
    .catch((err) => {
      throw new Error(err);
    });
};
export default i18n;

configureI18n({ i18n, it, en, fr, de, sl });
