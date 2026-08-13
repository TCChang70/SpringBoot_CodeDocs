import { get, post } from './client';

export const authApi = {
  login: (username, password) => post('/api/auth/login', { username, password }),
  register: (username, password, role) => post('/api/auth/register', { username, password, role }),
  me: () => get('/api/auth/me'),
};
