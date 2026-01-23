import type { InternalAxiosRequestConfig } from 'axios';
import { getAccessToken } from '../../context/AuthInterceptorContext';

const AxiosRequestTokenInterceptor = (config: InternalAxiosRequestConfig) => {
  const token = getAccessToken();
  if (token) {
    return {
      ...config,
      headers: config.headers.concat({
        Authorization: `Bearer ${token}`,
      }),
    };
  }

  return config;
};

export default AxiosRequestTokenInterceptor;
