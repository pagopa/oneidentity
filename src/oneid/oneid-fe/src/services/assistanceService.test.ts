import { describe, it, expect, vi } from 'vitest';
import { buildAssistanceURI } from './assistanceService';
import { trackEvent } from './analyticsService';

vi.mock('./analyticsService', () => ({
  trackEvent: vi.fn(),
}));

describe('buildAssistanceURI', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should return a mailto URI for a valid email address', () => {
    const email = 'support@example.com';
    const result = buildAssistanceURI(email);

    expect(result).toBe(`mailto:${email}`);
    expect(trackEvent).toHaveBeenCalledWith('CUSTOMER_CARE_MAILTO');
  });

  it('should return the same string for a valid URL', () => {
    const url = 'https://example.com/support';
    const result = buildAssistanceURI(url);

    expect(result).toBe(url);
    expect(trackEvent).toHaveBeenCalledWith('CUSTOMER_CARE_LINK');
  });

  it('should return the same string for an invalid email or URL', () => {
    const invalidString = 'invalid_string';
    const result = buildAssistanceURI(invalidString);

    expect(result).toBeNull();
    expect(trackEvent).toHaveBeenCalledWith('CUSTOMER_CARE_INVALID_LINK');
  });

  it('should handle undefined input gracefully', () => {
    const result = buildAssistanceURI();

    expect(result).toBeNull();
    expect(trackEvent).toHaveBeenCalledWith('CUSTOMER_CARE_INVALID_LINK');
  });

  it('should return a mailto URI for an email with subdomains', () => {
    const email = 'support@sub.example.com';
    const result = buildAssistanceURI(email);

    expect(result).toBe(`mailto:${email}`);
    expect(trackEvent).toHaveBeenCalledWith('CUSTOMER_CARE_MAILTO');
  });

  it('should return the same string for a URL with query parameters', () => {
    const url = 'https://example.com/support?query=123';
    const result = buildAssistanceURI(url);

    expect(result).toBe(url);
    expect(trackEvent).toHaveBeenCalledWith('CUSTOMER_CARE_LINK');
  });

  it('should return the same string for a URL with unusual characters', () => {
    const url = 'https://example.com/support#section';
    const result = buildAssistanceURI(url);

    expect(result).toBe(url);
    expect(trackEvent).toHaveBeenCalledWith('CUSTOMER_CARE_LINK');
  });
});
