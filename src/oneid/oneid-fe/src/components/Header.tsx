import { Fragment } from 'react';
import { HeaderProduct } from '@pagopa/mui-italia/dist/components/HeaderProduct/HeaderProduct';
import { HeaderAccount } from '@pagopa/mui-italia/dist/components/HeaderAccount/HeaderAccount';
import {
  RootLinkType,
  JwtUser,
  UserAction,
  ProductSwitchItem,
  ProductEntity,
} from '@pagopa/mui-italia';
import { PartySwitchItem } from '@pagopa/mui-italia/dist/components/PartySwitch';
import { ENV } from '../utils/env';
import { buildAssistanceURI } from '../services/assistanceService';
import { useLoginData } from '../hooks/useLoginData';
import { mapClientToProduct } from '../utils/utils';
import { ImageWithFallback } from './ImageFallback';
import { IDP_PLACEHOLDER_IMG } from '../utils/constants';

type PartyEntity = PartySwitchItem;
type HeaderProps = {
  /** If true, it will render an other toolbar under the Header */
  withSecondHeader: boolean;
  /** The list of products in header */
  productsList?: Array<ProductEntity>;
  /** The party id selected */
  selectedPartyId?: string;
  /** The product id selected */
  selectedProductId?: string;
  /** The parties list */
  partyList?: Array<PartyEntity>;
  /** The logged user or false if there is not a valid session */
  loggedUser: JwtUser | false;
  /** The email to which the assistance button will ask to send an email, if the user is not logged in, otherwise it will be redirect to the assistance form */
  assistanceEmail?: string;
  /** The function invoked when the user click on a product */
  onSelectedProduct?: (product: ProductSwitchItem) => void;
  /** The function invoked when the user click on a party from the switch  */
  onSelectedParty?: (party: PartySwitchItem) => void;
  /** The function to be invoked when pressing the rendered logout button, if not defined it will redirect to the logout page, if setted to null it will no render the logout button. It's possible to modify the logout path changing the value in CONFIG.logout inside the index.tsx file */
  onExit?: (exitAction: () => void) => void;
  /** If false hides login button  */
  enableLogin?: boolean;
  /** The users actions inside the user dropdown. It's visible only if enableLogin and enableDropdown are true */
  userActions?: Array<UserAction>;
  /** If true the user dropdown in headerAccount component is visible. It's visible only if enableLogin is true */
  enableDropdown?: boolean;
  /* The number of characters beyond which the multiLine is applied in component PartyAccountItemButton */
  maxCharactersNumberMultiLineButton?: number;
  /* The number of characters beyond which the multiLine is applied in component PartyAccountItem */
  maxCharactersNumberMultiLineItem?: number;
  /** If false hides assistance button */
  enableAssistanceButton?: boolean;
  /** A callback function that controls the visibility of the documentation button and handles user clicks on the documentation button. */
  onDocumentationClick?: () => void;
};

const rootLink: RootLinkType = {
  label: 'PagoPA S.p.A.',
  href: ENV.HEADER.LINK.PAGOPALINK,
  ariaLabel: 'Link: vai al sito di PagoPA S.p.A.',
  title: 'Sito di PagoPA S.p.A.',
};

/** Header component */
const Header = ({
  withSecondHeader,
  selectedPartyId,
  partyList = [],
  loggedUser,
  assistanceEmail,
  enableLogin = true,
  userActions = [],
  enableDropdown = false,
  onExit = (exitAction) => exitAction(),
  onSelectedProduct,
  onSelectedParty,
  maxCharactersNumberMultiLineButton,
  maxCharactersNumberMultiLineItem,
  enableAssistanceButton = true,
  onDocumentationClick,
}: HeaderProps) => {
  const { clientQuery } = useLoginData();
  const getClientLogo = () => (
    <>
      {clientQuery.isFetched && (
        <ImageWithFallback
          style={{
            width: '100%',
            maxWidth: '48px',
            maxHeight: '48px',
            objectFit: 'cover',
          }}
          src={clientQuery.data?.logoUri}
          alt={clientQuery.data?.friendlyName || 'PagoPa Logo'}
          placeholder={IDP_PLACEHOLDER_IMG}
        />
      )}
    </>
  );
  const product = mapClientToProduct(clientQuery.data, getClientLogo());

  return (
    <Fragment>
      <header>
        <HeaderAccount
          rootLink={rootLink}
          loggedUser={loggedUser}
          onAssistanceClick={() =>
            onExit(() =>
              window.location.assign(buildAssistanceURI(assistanceEmail))
            )
          }
          onLogin={() => onExit(() => window.location.assign(ENV.URL_FE.LOGIN))}
          onLogout={() =>
            onExit(() => window.location.assign(ENV.URL_FE.LOGOUT))
          }
          enableLogin={enableLogin}
          userActions={userActions}
          enableDropdown={enableDropdown}
          enableAssistanceButton={enableAssistanceButton}
          onDocumentationClick={onDocumentationClick}
        />
      </header>
      {withSecondHeader === true && product ? (
        <nav>
          <HeaderProduct
            productId={product?.id}
            productsList={[product]}
            partyId={selectedPartyId}
            partyList={partyList}
            onSelectedProduct={onSelectedProduct}
            onSelectedParty={onSelectedParty}
            maxCharactersNumberMultiLineButton={
              maxCharactersNumberMultiLineButton
            }
            maxCharactersNumberMultiLineItem={maxCharactersNumberMultiLineItem}
          />
        </nav>
      ) : (
        ''
      )}
    </Fragment>
  );
};

export default Header;
