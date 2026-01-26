import axios from 'axios';
import AxiosRequestTokenInterceptor from './AxiosRequestTokenInterceptor';

const AxiosBase = axios.create({
  headers: {
    'Content-Type': 'application/json',
  },
});

AxiosBase.interceptors.request.use(AxiosRequestTokenInterceptor, (error) => {
  return Promise.reject(error);
});

export default AxiosBase;
