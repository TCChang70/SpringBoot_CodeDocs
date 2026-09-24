// 分類 API（後端 §7）— 樹狀結構、受限刪除
import { api } from './client.js';

// 公開分類樹（巢狀 children）
export const tree = () => api('/categories');

export const create = (payload) => api('/categories', { method: 'POST', body: payload });

export const update = (id, payload) => api(`/categories/${id}`, { method: 'PUT', body: payload });

// 刪除：有文章時須帶 moveToCategoryId 將文章移轉後才可刪
export const remove = (id, moveToCategoryId) => {
  const qs = moveToCategoryId != null ? `?moveToCategoryId=${moveToCategoryId}` : '';
  return api(`/categories/${id}${qs}`, { method: 'DELETE' });
};

// 把巢狀樹攤平成 [{id,name,depth,slug}]，方便下拉選單使用
export function flattenTree(nodes, depth = 0) {
  const out = [];
  for (const n of nodes) {
    out.push({ id: n.id, name: n.name, slug: n.slug, depth });
    if (n.children?.length) out.push(...flattenTree(n.children, depth + 1));
  }
  return out;
}