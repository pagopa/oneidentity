import type { InternalAxiosRequestConfig } from 'axios';
import { getIdToken } from '../../context/AuthInterceptorContext';

const AxiosRequestTokenInterceptor = (config: InternalAxiosRequestConfig) => {
  const token = getIdToken();
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
