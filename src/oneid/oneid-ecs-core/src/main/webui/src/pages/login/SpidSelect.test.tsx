import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';

import { ENV } from '../../utils/env';
import SpidSelect from './SpidSelect';

const oldWindowLocation = global.window.location;
beforeAll(() => {
  // eslint-disable-next-line functional/immutable-data
  Object.defineProperty(window, 'location', { value: { assign: vi.fn() } });
});
afterAll(() => {
  // eslint-disable-next-line functional/immutable-data
  Object.defineProperty(window, 'location', { value: oldWindowLocation });
});
const idpList = {
  identityProviders: [
    { identifier: 'test', entityID: 'testID', name: 'test', imageUrl: 'test' },
  ],
  richiediSpid: '',
};
test('go to the spid url', () => {
  render(<SpidSelect onBack={() => null} idpList={idpList} />);

  idpList.identityProviders.forEach((element) => {
    const spidImg = screen.getByAltText(element.name);
    const spidSpan = spidImg.parentNode;
    const spidButton = spidSpan?.parentNode;
    fireEvent.click(spidButton as Element);
    const id = element.entityID;
    expect(global.window.location.assign).toHaveBeenCalledWith(
      ENV.URL_API.AUTHORIZE + '?idp=' + encodeURIComponent(id)
    );
  });
});
