import { ENV } from '../utils/env';

/** To call in order to start the analytics service, otherwise no event will be sent */
export const initAnalytics = (): void => {
  if (ENV.ANALYTCS.ENABLE) {
    // to be defined
  }
};

/** To notify an error through the analytics tool */
export const trackAppError = (error: string): void => {
  if (ENV.ANALYTCS.ENABLE) {
    trackEvent('GENERIC_ERROR', error);
  } else {
    console.error(error);
  }
};

/**
 * To notify an event through the analytics tool:
 * @property event_name: the name of the event
 * @property properties: the additional payload sent with the event
 * @property callback: an action taken when the track has completed (If the action taken immediately after the track is an exit action from the application, it's better to use this callback to perform the exit, in order to give to mixPanel the time to send the event)
 */
export const trackEvent = (event_name: string, properties?: any, callback?: () => void): void => {
  if (ENV.ANALYTCS.ENABLE) {
    if (ENV.ANALYTCS.MOCK) {
       
      console.log(event_name, properties);
      if (callback) {
        callback();
      }
    } else {
      trackEventThroughAnalyticTool(event_name, properties, callback);
    }
  } else {
    if (callback) {
      callback();
    }
  }
};

const trackEventThroughAnalyticTool = (
  event_name: string,
  properties?: any,
  _?: () => void
): void => {
  console.log('trackEvent', event_name, properties);
  // to be defined
};
