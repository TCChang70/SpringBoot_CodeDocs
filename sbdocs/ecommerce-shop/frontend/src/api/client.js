const BASE_URL = import.meta.env.VITE_API_BASE || '';
const TOKEN_KEY = 'ecom_token';
const USER_KEY = 'ecom_user';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  window.dispatchEvent(new Event('auth:expired'));
}

async function request(path, options = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

  if (res.status === 204) return null;

  const text = await res.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }

  if (res.status === 401 && !path.startsWith('/api/auth')) {
    clearAuth();
    throw new Error('登入已過期，請重新登入');
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
