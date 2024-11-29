import { test, expect } from '@playwright/test';

test('has title', async ({ page }) => {
  await page.goto('/login');
  // Expect title "to contain" substring
  await expect(page).toHaveTitle(/PagoPA One Identity – Login/);
});
