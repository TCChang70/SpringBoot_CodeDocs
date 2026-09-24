// 文章 API（後端 §9）— 前台公開 + 作者/審核 + 後台管理
import { api } from './client.js';

// 前台：已發佈文章列表（分類/標籤/關鍵字/分頁）
export const list = (params) => api(`/articles?${new URLSearchParams(params)}`);
export const detail = (id) => api(`/articles/${id}`);

// 作者/審核（登入）
export const create = (payload) => api('/articles', { method: 'POST', body: payload });
export const update = (id, payload) => api(`/articles/${id}`, { method: 'PUT', body: payload });
export const remove = (id) => api(`/articles/${id}`, { method: 'DELETE' });
export const getManage = (id) => api(`/articles/${id}/manage`); // 含 content

// 狀態機動作（後端 §9.7）
export const submit = (id) => api(`/articles/${id}/submit`, { method: 'POST' });
export const approve = (id) => api(`/articles/${id}/approve`, { method: 'POST' });
export const reject = (id) => api(`/articles/${id}/reject`, { method: 'POST' });
export const archive = (id) => api(`/articles/${id}/archive`, { method: 'POST' });
export const publish = (id) => api(`/articles/${id}/publish`, { method: 'POST' });

// 後台（admin/editor 或本人）
export const adminList = (params) => api(`/admin/articles?${new URLSearchParams(params)}`);
export const mine = (params) => api(`/admin/articles/mine?${new URLSearchParams(params)}`);
export const pendingList = (params) => api(`/admin/articles/pending?${new URLSearchParams(params)}`);