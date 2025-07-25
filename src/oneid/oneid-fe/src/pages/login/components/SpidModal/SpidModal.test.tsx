import { render, screen, fireEvent } from '@testing-library/react';

import { IdentityProviders } from '../../../../utils/IDPS';
import { trackEvent } from '../../../../services/analyticsService';
import { forwardSearchParams } from '../../../../utils/utils';
import SpidModal from '.././SpidModal';

vi.mock('../../../../services/analyticsService');
vi.mock('../../../../utils/utils', () => ({
  forwardSearchParams: vi.fn(() => 'testParams'),
}));

const mockIdpList = {
  identityProviders: [
    {
      active: true,
      entityID: 'idp1',
      friendlyName: 'IDP 1',
      imageUrl: 'image1.png',
    },
    {
      active: true,
      entityID: 'idp2',
      friendlyName: 'IDP 2',
      imageUrl: 'image2.png',
    },
  ],
} as IdentityProviders;

describe('SpidModal', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders the modal with identity providers', () => {
    render(
      <SpidModal
        loading={false}
        openSpidModal={true}
        setOpenSpidModal={vi.fn()}
        idpList={mockIdpList}
      />
    );

    expect(screen.getByText('spidSelect.modalTitle')).toBeInTheDocument();
    expect(screen.getByTestId('idp-button-idp1')).toBeInTheDocument();
    expect(screen.getByTestId('idp-button-idp2')).toBeInTheDocument();
  });

  it('calls getSPID on button click', () => {
    render(
      <SpidModal
        openSpidModal={true}
        setOpenSpidModal={vi.fn()}
        idpList={mockIdpList}
      />
    );

    const button = screen.getByTestId('idp-button-idp1');
    fireEvent.click(button);

    expect(forwardSearchParams).toHaveBeenCalledWith('idp1');
    expect(trackEvent).toHaveBeenCalledWith(
      'LOGIN_IDP_SELECTED',
      {
        SPID_IDP_NAME: 'IDP 1',
        SPID_IDP_ID: 'idp1',
        FORWARD_PARAMETERS: 'testParams',
      },
      expect.any(Function)
    );
  });

  it('closes modal on close button click', () => {
    const setOpenSpidModal = vi.fn();
    render(
      <SpidModal
        openSpidModal={true}
        setOpenSpidModal={setOpenSpidModal}
        idpList={mockIdpList}
      />
    );

    const closeButton = screen.getByTestId('close-button');
    fireEvent.click(closeButton);

    expect(setOpenSpidModal).toHaveBeenCalledWith(false);
  });
});
