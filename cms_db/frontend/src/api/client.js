// 統一 API 呼叫封裝（src/api/client.js）
//
// 後端慣例（backend-API教學文件 §3）：
//   - 成功回應 { code:0, message:"success", data:... }
//   - 錯誤回應 { code:<非0錯誤碼>, message:"說明", data:null } + 對應 HTTP 狀態
//   - 受保護端點需帶 Authorization: Bearer <token>

// 後端所有端點掛在 /api/v1 下；開發時相對路徑交給 Vite proxy 轉送。
// 跨主機部署才需要以 VITE_API_BASE 覆寫為絕對位址（如 http://api.example.com/api/v1）。
const BASE = import.meta.env.VITE_API_BASE || '/api/v1';

export const TOKEN_KEY = 'cms_token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export class ApiError extends Error {
  constructor(code, message, status) {
    super(message);
    this.name = 'ApiError';
    this.code = code; // 後端錯誤碼（對照 backend-API教學文件 附錄 A）
    this.status = status; // HTTP 狀態碼
  }
}

/**
 * 送出請求並解開 ApiResponse 封裝，成功時回傳 data。
 * @param {string} path  例如 "/articles?page=0&size=10"
 * @param {object} opts  method / body(物件，自動 JSON) / formData(FormData) / token(覆寫)
 */
export async function api(path, { method = 'GET', body, formData, token } = {}) {
  const headers = {};
  let payload;

  if (formData) {
    payload = formData; // 瀏覽器自動加 multipart boundary 與 Content-Type
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(body);
  }

  const t = token || getToken();
  if (t) headers['Authorization'] = `Bearer ${t}`;

  let res;
  try {
    res = await fetch(BASE + path, { method, headers, body: payload });
  } catch {
    throw new ApiError(0, '無法連線到伺服器，請確認後端已啟動', 0);
  }

  let data = null;
  try {
    data = await res.json();
  } catch {
    // 非 JSON 回應（理論上後端都會回 JSON）
  }

  if (!res.ok) {
    const code = data?.code ?? 0;
    const message = data?.message ?? res.statusText;
    // 未登入（40010）且不是「正停在登入頁」→ 清除 token 並導回登入頁。
    // 已在登入頁時不跳轉（由 AuthContext 清除失效 token），避免誤導訊息與多餘重整。
    // 密碼錯誤為 40011，不會命中這裡，會原樣顯示「帳號或密碼錯誤」。
    if (res.status === 401 && code === 40010 && !window.location.pathname.startsWith('/login')) {
      setToken(null);
      window.location.href = '/login?reason=session';
    }
    throw new ApiError(code, message, res.status);
  }

  // 後端成功時一律回 JSON（ApiResponse 封裝）；若是代理/未就緒回應的非 JSON，視為異常
  if (data === null) {
    throw new ApiError(0, '伺服器回應格式異常（非 JSON），請確認後端已啟動', res.status);
  }
  return data.data;
}