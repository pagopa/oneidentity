import { afterEach, describe, expect, it, vi } from 'vitest';

describe('CIE test environment selection', () => {
  afterEach(() => {
    vi.unstubAllEnvs();
    vi.resetModules();
  });

  it.each([
    ['dev', true, false, true],
    ['uat', true, true, false],
    ['io-uat', true, true, true],
    ['prod', false, false, false],
    ['io-prod', false, false, false],
    ['production', false, false, false],
    ['development', false, false, false],
    ['test', false, false, false],
    ['unknown', false, false, false],
  ])(
    'configures CIE test environments in %s',
    async (mode, selectionEnabled, preproductionEnabled, testingEnabled) => {
      vi.stubEnv('MODE', mode);
      vi.resetModules();

      const { ENV } = await import('./env');

      expect(ENV.CIE_TEST_ENV_ENABLED).toBe(selectionEnabled);
      expect(ENV.CIE_TEST_ENVIRONMENT_ENABLED).toEqual({
        PREPRODUCTION: preproductionEnabled,
        TESTING: testingEnabled,
      });
    }
  );
});
