// 多媒體 API（後端 §11）
import { api } from './client.js';

// 媒體庫列表（admin/editor 看全部，author 只看自己的）
export const list = (params) => api(`/media?${new URLSearchParams(params)}`);

// 上傳 multipart（欄位名 file）
export const upload = (file) => {
  const fd = new FormData();
  fd.append('file', file);
  return api('/media', { method: 'POST', formData: fd });
};

export const updateAltText = (id, altText) =>
  api(`/media/${id}${altText != null ? `?altText=${encodeURIComponent(altText)}` : ''}`, { method: 'PUT' });

export const remove = (id) => api(`/media/${id}`, { method: 'DELETE' });

// 後端回傳的 url 是根路徑相對位址（如 /media/files/5），
// 開發時由 Vite proxy 轉送；跨主機部署請用 VITE_MEDIA_BASE 指定後端來源。
export const mediaUrl = (u) =>
  u && !u.startsWith('http') ? `${import.meta.env.VITE_MEDIA_BASE || ''}${u}` : u;