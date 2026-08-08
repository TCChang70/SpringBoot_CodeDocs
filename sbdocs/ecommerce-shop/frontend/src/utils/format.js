export function formatMoney(value) {
  if (value === null || value === undefined) return '-';
  return `NT$${Number(value).toLocaleString('zh-TW', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  })}`;
}

export function formatDateTime(value) {
  if (!value) return '-';
  return String(value).replace('T', ' ').slice(0, 19);
}

export function calcTotal(items) {
  return Object.values(items).reduce(
    (sum, item) => sum + (item?.price || 0) * (item?.quantity || 0),
    0,
  );
}
