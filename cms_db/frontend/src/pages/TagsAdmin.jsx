// 標籤管理（後端 §8）
import { useEffect, useState } from 'react';
import * as tagsApi from '../api/tags.js';
import Alert from '../components/Alert.jsx';

export default function TagsAdmin() {
  const [tags, setTags] = useState([]);
  const [error, setError] = useState(null);
  const [form, setForm] = useState({ name: '', slug: '' });
  const [editingId, setEditingId] = useState(null);

  const load = () => tagsApi.list().then(setTags).catch((e) => setError(e.message));
  useEffect(() => { load(); }, []);

  const submit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      if (editingId == null) {
        await tagsApi.create(form);
      } else {
        await tagsApi.update(editingId, form);
      }
      setForm({ name: '', slug: '' });
      setEditingId(null);
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  const startEdit = (t) => {
    setEditingId(t.id);
    setForm({ name: t.name, slug: t.slug });
  };

  const remove = async (id, name) => {
    if (!window.confirm(`確定刪除標籤「${name}」？`)) return;
    try {
      await tagsApi.remove(id);
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div>
      <div className="page-title"><h1>標籤管理</h1></div>
      {error && <Alert variant="error">{error}</Alert>}

      <div className="card">
        <h3>{editingId == null ? '新增標籤' : '編輯標籤'}</h3>
        <form onSubmit={submit}>
          <div className="form-row">
            <div className="field">
              <label>名稱 *（≤50）</label>
              <input className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </div>
            <div className="field">
              <label>slug *（≤50）</label>
              <input className="input" value={form.slug} onChange={(e) => setForm({ ...form, slug: e.target.value })} />
            </div>
          </div>
          <div className="actions">
            <button type="submit" className="btn btn-primary">{editingId == null ? '新增' : '儲存變更'}</button>
            {editingId != null && (
              <button type="button" className="btn" onClick={() => { setEditingId(null); setForm({ name: '', slug: '' }); }}>取消</button>
            )}
          </div>
        </form>
      </div>

      <div className="card">
        <table className="table">
          <thead><tr><th>名稱</th><th>slug</th><th>文章數</th><th>操作</th></tr></thead>
          <tbody>
            {(tags || []).map((t) => (
              <tr key={t.id}>
                <td>{t.name}</td>
                <td className="muted">{t.slug}</td>
                <td>{t.articleCount}</td>
                <td>
                  <div className="actions">
                    <button className="btn btn-sm" onClick={() => startEdit(t)}>編輯</button>
                    <button className="btn btn-sm btn-danger" onClick={() => remove(t.id, t.name)}>刪除</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}