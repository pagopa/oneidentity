import { expect, test } from '@playwright/test';
import { TEST_IDP_ID, TEST_LOGIN_URL } from './utils/constants';

test('auth is successful', async ({ page }) => {
  test.slow();
  const nonce = process.env.NONCE as string;
  await page.goto(TEST_LOGIN_URL);

  await page.locator('#spidButton').click();
  await page.locator(`[id="${TEST_IDP_ID}"]`).click();
  await page.waitForURL('**/client/cb?*', { timeout: 30000 });
  expect(page.getByText(nonce)).toBeTruthy();
  expect(page.getByText(nonce)).toBeVisible({ timeout: 30000 });
});
