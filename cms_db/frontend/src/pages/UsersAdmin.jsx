// 使用者管理（僅 admin，後端 §6.4~6.7）
// 角色變更 / 停用啟用 / 刪除（有文章的使用者後端會拒絕刪除）
import { useEffect, useState } from 'react';
import * as usersApi from '../api/users.js';
import Pagination from '../components/Pagination.jsx';
import Alert from '../components/Alert.jsx';
import { ROLE_LABELS, ROLE_OPTIONS } from '../constants.js';

export default function UsersAdmin() {
  const [data, setData] = useState(null);
  const [page, setPage] = useState(0);
  const [error, setError] = useState(null);
  const [form, setForm] = useState({ username: '', email: '', password: '', displayName: '', role: 'author' });

  const load = (p) => usersApi.listUsers({ page: p, size: 10, sort: 'id,asc' }).then(setData).catch((e) => setError(e.message));

  useEffect(() => {
    load(page);
  }, [page]);

  const createAccount = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const payload = {
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
        displayName: form.displayName.trim() || null,
        role: form.role,
      };
      await usersApi.createUser(payload);
      setForm({ username: '', email: '', password: '', displayName: '', role: 'author' });
      load(page);
    } catch (err) {
      setError(err.message);
    }
  };

  const changeRole = async (u, role) => {
    if (role === u.role) return;
    try {
      await usersApi.adminUpdate(u.id, { role });
      setError(null);
    } catch (e) {
      setError(e.message);
    }
  };

  const toggleEnabled = async (u) => {
    try {
      await usersApi.setEnabled(u.id, !u.enabled);
      load(page);
    } catch (e) {
      setError(e.message);
    }
  };

  const remove = async (u) => {
    if (!window.confirm(`確定刪除使用者「${u.username}」？`)) return;
    try {
      await usersApi.removeUser(u.id);
      load(page);
    } catch (e) {
      setError(e.message); // 例如「使用者尚有文章，請改用停用」(40906)
    }
  };

  return (
    <div>
      <div className="page-title"><h1>使用者管理</h1></div>
      {error && <Alert variant="error">{error}</Alert>}

      <div className="card">
        <h3>建立新帳戶</h3>
        <form onSubmit={createAccount}>
          <div className="form-row">
            <div className="field">
              <label>帳號 *（3~50）</label>
              <input className="input" value={form.username} minLength={3} maxLength={50} onChange={(e) => setForm({ ...form, username: e.target.value })} required />
            </div>
            <div className="field">
              <label>信箱 *</label>
              <input className="input" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
            </div>
            <div className="field">
              <label>密碼 *（8~72）</label>
              <input className="input" type="password" value={form.password} minLength={8} maxLength={72} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
            </div>
          </div>
          <div className="form-row">
            <div className="field">
              <label>顯示名稱（選填）</label>
              <input className="input" value={form.displayName} onChange={(e) => setForm({ ...form, displayName: e.target.value })} />
            </div>
            <div className="field">
              <label>角色 *</label>
              <select className="select" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                {ROLE_OPTIONS.map((r) => (
                  <option key={r} value={r}>{ROLE_LABELS[r]}</option>
                ))}
              </select>
            </div>
            <div className="field" style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button type="submit" className="btn btn-primary">建立帳戶</button>
            </div>
          </div>
        </form>
      </div>

      <div className="card">
        <table className="table">
          <thead>
            <tr><th>ID</th><th>帳號</th><th>信箱</th><th>顯示名稱</th><th>角色</th><th>狀態</th><th>操作</th></tr>
          </thead>
          <tbody>
            {data?.content?.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.username}</td>
                <td className="muted">{u.email}</td>
                <td>{u.displayName || '-'}</td>
                <td>
                  <select className="select" style={{ padding: 4 }} value={u.role} onChange={(e) => changeRole(u, e.target.value)}>
                    {ROLE_OPTIONS.map((r) => (
                      <option key={r} value={r}>{ROLE_LABELS[r]}</option>
                    ))}
                  </select>
                </td>
                <td>{u.enabled ? <span className="badge badge-published">啟用</span> : <span className="badge badge-spam">停用</span>}</td>
                <td>
                  <div className="actions">
                    <button className="btn btn-sm btn-warn" onClick={() => toggleEnabled(u)}>停用 / 啟用</button>
                    <button className="btn btn-sm btn-danger" onClick={() => remove(u)}>刪除</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Pagination page={data?.page ?? 0} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements} onChange={setPage} />
    </div>
  );
}