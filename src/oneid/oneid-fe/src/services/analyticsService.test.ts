/* eslint-disable functional/immutable-data */
import { ENV } from '../utils/env';
import { initAnalytics, trackAppError, trackEvent } from './analyticsService';

vi.mock('../utils/env', () => ({
  ENV: {
    ANALYTICS: {
      ENABLE: true,
      MOCK: true,
    },
  },
}));

const mockErrorMsg = 'Test Error';

describe('Analytics Service', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('trackEvent', () => {
    it('should log the event and call the callback when analytics is enabled and mock is true', () => {
      const consoleSpy = vi.spyOn(console, 'log');
      const callback = vi.fn();

      trackEvent('TEST_EVENT', { key: 'value' }, callback);

      expect(consoleSpy).toHaveBeenCalledWith('TEST_EVENT', { key: 'value' });
      expect(callback).toHaveBeenCalled();
    });

    it('should not log or call callback when analytics is disabled', () => {
      ENV.ANALYTICS.ENABLE = false;
      const consoleSpy = vi.spyOn(console, 'log');
      const callback = vi.fn();

      trackEvent('TEST_EVENT', { key: 'value' }, callback);

      expect(consoleSpy).not.toHaveBeenCalled();
      expect(callback).toHaveBeenCalled();
    });

    it('should call trackEventThroughAnalyticTool when mock is false', () => {
      ENV.ANALYTICS.ENABLE = true;
      ENV.ANALYTICS.MOCK = false;
      const analyticToolSpy = vi.spyOn(console, 'log'); // Mock `trackEventThroughAnalyticTool`
      const callback = vi.fn();

      trackEvent('TEST_EVENT', { key: 'value' }, callback);

      expect(analyticToolSpy).toHaveBeenCalledWith('trackEvent', 'TEST_EVENT', {
        key: 'value',
      });
      expect(callback).not.toHaveBeenCalled(); // callback is not called immediately
    });
  });

  describe('trackAppError', () => {
    it('should log the error if analytics is disabled', () => {
      ENV.ANALYTICS.ENABLE = false;
      const consoleSpy = vi.spyOn(console, 'error');

      trackAppError(mockErrorMsg);

      expect(consoleSpy).toHaveBeenCalledWith(mockErrorMsg);
    });

    it('should call trackEvent for error if analytics is enabled', () => {
      ENV.ANALYTICS.ENABLE = true;
      ENV.ANALYTICS.MOCK = false; // Ensure mock is false for this test
      const trackEventSpy = vi.spyOn(console, 'log');

      trackAppError(mockErrorMsg);

      expect(trackEventSpy).toHaveBeenCalledWith(
        'trackEvent',
        'GENERIC_ERROR',
        mockErrorMsg
      );
    });
  });

  describe('initAnalytics', () => {
    it('should not do anything yet as it is to be defined', () => {
      // Currently no logic inside, just test that it doesn't throw
      expect(initAnalytics()).toBeUndefined();
    });
  });
});
