export const productId2ProductTitle = (productId: string) => {
  const productMap = {
    'prod-io': 'App IO',
    'prod-pn': 'SEND - Servizio Notifiche Digitali',
    'prod-interop': 'Interoperabilità',
    'prod-pagopa': 'Piattaforma pagoPA',
    'prod-idpay': 'IDPay',
    'prod-cgn': 'Carta Giovani',
    'prod-io-sign': 'Firma con IO',
    'prod-fd': 'Fideiussioni Digitali',
  };
  return productMap[productId as keyof typeof productMap];
};
