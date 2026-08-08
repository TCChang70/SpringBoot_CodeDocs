const BASE_URL = import.meta.env.VITE_API_BASE || '';

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (res.status === 204) return null;
  const text = await res.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }
  if (!res.ok) {
    const message = typeof data === 'string' ? data : data?.message || res.statusText;
    throw new Error(message || `HTTP ${res.status}`);
  }
  return data;
}

export const get = (path) => request(path);
export const post = (path, body) =>
  request(path, {
    method: 'POST',
    body: body === undefined ? undefined : JSON.stringify(body),
  });
export const put = (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) });
export const del = (path) => request(path, { method: 'DELETE' });
