// 認證 API（後端 §5）
import { api } from './client.js';

// 註冊 → 預設角色 author（FR-01-04）
export const register = (payload) => api('/auth/register', { method: 'POST', body: payload });

// 登入 → 回傳 { token, tokenType, expiresInMs, user }
export const login = (payload) => api('/auth/login', { method: 'POST', body: payload });