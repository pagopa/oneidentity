import { afterEach, describe, expect, it, vi } from 'vitest';

describe('CIE test environment selection', () => {
  afterEach(() => {
    vi.unstubAllEnvs();
    vi.resetModules();
  });

  it.each([
    ['dev', true],
    ['uat', true],
    ['io-uat', true],
    ['prod', false],
    ['io-prod', false],
    ['production', false],
    ['development', false],
    ['test', false],
    ['unknown', false],
  ])('enables selection in %s: %s', async (mode, enabled) => {
    vi.stubEnv('MODE', mode);
    vi.resetModules();

    const { ENV } = await import('./env');

    expect(ENV.CIE_TEST_ENV_ENABLED).toBe(enabled);
  });
});
