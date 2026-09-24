// 留言 API（後端 §10）— 前台 + 管理審核
import { api } from './client.js';

// 前台：僅已核准留言（巢狀 children）
export const list = (articleId) => api(`/articles/${articleId}/comments`);
export const create = (articleId, payload) => api(`/articles/${articleId}/comments`, { method: 'POST', body: payload });

// 管理審核（admin/editor）
export const adminList = (params) => api(`/admin/comments?${new URLSearchParams(params)}`);
export const approve = (id) => api(`/admin/comments/${id}`, { method: 'PATCH' });
export const markSpam = (id) => api(`/admin/comments/${id}/spam`, { method: 'PATCH' });
export const restore = (id) => api(`/admin/comments/${id}/restore`, { method: 'PATCH' });
export const remove = (id) => api(`/admin/comments/${id}`, { method: 'DELETE' });