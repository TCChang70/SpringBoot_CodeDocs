// 使用者 API（後端 §6）
import { api } from './client.js';

export const me = () => api('/users/me');

export const updateProfile = (payload) => api('/users/me', { method: 'PUT', body: payload });

export const changePassword = (payload) => api('/users/me/password', { method: 'PUT', body: payload });

// admin
export const listUsers = (params) => api(`/users?${new URLSearchParams(params)}`);
export const createUser = (payload) => api('/users', { method: 'POST', body: payload });
export const adminUpdate = (id, payload) => api(`/users/${id}`, { method: 'PUT', body: payload });
export const setEnabled = (id, enabled) => api(`/users/${id}/${enabled ? 'enable' : 'disable'}`, { method: 'PATCH' });
export const removeUser = (id) => api(`/users/${id}`, { method: 'DELETE' });