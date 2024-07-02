import React from 'react';
import { render } from '@testing-library/react';
import Logout from '../Logout';
import { ROUTE_LOGIN } from '../../../utils/constants';

const oldWindowLocation = global.window.location;

beforeAll(() => {
  Object.defineProperty(window, 'location', { value: { assign: jest.fn() } });
});
afterAll(() => {
  Object.defineProperty(window, 'location', { value: oldWindowLocation });
});

test('test logout', () => {
  render(<Logout />);

  expect(global.window.location.assign).toHaveBeenCalledWith(ROUTE_LOGIN);
});
