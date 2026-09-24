export function formatDate(iso) {
  if (!iso) return '-';
  const d = new Date(iso);
  return d.toLocaleString('zh-TW', { hour12: false });
}

export function slugify(text) {
  return (text || '')
    .toLowerCase()
    .trim()
    .replace(/[^\w\u4e00-\u9fff-]+/g, '-') // 中文外一律轉成連字號
    .replace(/-+/g, '-')
    .replace(/^-+|-+$/g, '');
}