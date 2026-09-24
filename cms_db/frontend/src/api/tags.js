// 標籤 API（後端 §8）
import { api } from './client.js';

export const list = () => api('/tags');
export const create = (payload) => api('/tags', { method: 'POST', body: payload });
export const update = (id, payload) => api(`/tags/${id}`, { method: 'PUT', body: payload });
export const remove = (id) => api(`/tags/${id}`, { method: 'DELETE' });