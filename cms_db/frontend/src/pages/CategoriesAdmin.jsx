// 分類管理（後端 §7）— 樹狀顯示、新增/編輯（含循環防護）、受限刪除（移動文章）
import { useEffect, useMemo, useState } from 'react';
import * as categoriesApi from '../api/categories.js';
import Alert from '../components/Alert.jsx';

export default function CategoriesAdmin() {
  const [tree, setTree] = useState([]);
  const [error, setError] = useState(null);

  const [form, setForm] = useState({ name: '', slug: '', description: '', parentId: '' });
  const [editingId, setEditingId] = useState(null);

  const [deleting, setDeleting] = useState(null); // 待刪除的分類
  const [moveTo, setMoveTo] = useState('');

  const load = () => categoriesApi.tree().then(setTree).catch((e) => setError(e.message));
  useEffect(() => { load(); }, []);

  const flat = useMemo(() => categoriesApi.flattenTree(tree || []), [tree]);

  // 收集 selected 分類本身與其所有後代 id（編輯時不可把自己/後代當父分類 → 40905）
  const blockedIds = useMemo(() => {
    if (editingId == null) return new Set();
    const result = new Set([editingId]);
    const collect = (nodes) => {
      for (const n of nodes) {
        if (n.id === editingId || result.has(n.id)) {
          n.children?.forEach((child) => { result.add(child.id); collect([child]); });
        } else {
          collect(n.children || []);
        }
      }
    };
    collect(tree);
    return result;
  }, [editingId, tree]);

  const directChildren = (id) => {
    for (const n of tree) {
      const found = findNode(n, id);
      if (found) return found.children?.length || 0;
    }
    return 0;
  };
  const findNode = (node, id) => {
    if (node.id === id) return node;
    for (const child of node.children || []) {
      const r = findNode(child, id);
      if (r) return r;
    }
    return null;
  };

  const renderTree = (nodes, depth = 0) =>
    nodes.map((n) => (
      <div key={n.id}>
        <div className="tree-row">
          <span className="depth-spacer" style={{ width: depth * 24 }} />
          <strong>{n.name}</strong>
          <span className="muted">slug：{n.slug}</span>
          <span className="muted">{n.articleCount} 篇文章</span>
          {directChildren(n.id) > 0 && <span className="muted">（{directChildren(n.id)} 個子分類）</span>}
          <div className="actions" style={{ marginLeft: 'auto' }}>
            <button className="btn btn-sm" onClick={() => startEdit(n)}>編輯</button>
            <button className="btn btn-sm btn-danger" onClick={() => askDelete(n)}>刪除</button>
          </div>
        </div>
        {n.children?.length > 0 && renderTree(n.children, depth + 1)}
      </div>
    ));

  const startEdit = (n) => {
    setEditingId(n.id);
    setForm({ name: n.name, slug: n.slug, description: n.description || '', parentId: n.parentId ?? '' });
  };

  const askDelete = (n) => {
    setError(null);
    if (directChildren(n.id) > 0) {
      setError(`「${n.name}」尚有子分類，無法刪除。`);
      return;
    }
    setDeleting(n);
    setMoveTo('');
  };

  const submitDelete = async () => {
    try {
      await categoriesApi.remove(deleting.id, moveTo || null);
      setDeleting(null);
      load();
    } catch (e) {
      setError(e.message);
      setDeleting(null);
    }
  };

  const submit = async (e) => {
    e.preventDefault();
    setError(null);
    const payload = {
      name: form.name,
      slug: form.slug,
      description: form.description || null,
      parentId: form.parentId === '' ? null : Number(form.parentId),
    };
    try {
      if (editingId == null) {
        await categoriesApi.create(payload);
      } else {
        await categoriesApi.update(editingId, payload);
      }
      setForm({ name: '', slug: '', description: '', parentId: '' });
      setEditingId(null);
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div>
      <div className="page-title"><h1>分類管理</h1></div>
      {error && <Alert variant="error">{error}</Alert>}

      <div className="card">
        <h3>{editingId == null ? '新增分類' : '編輯分類'}</h3>
        <form onSubmit={submit}>
          <div className="form-row">
            <div className="field">
              <label>名稱 *</label>
              <input className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </div>
            <div className="field">
              <label>slug *</label>
              <input className="input" value={form.slug} onChange={(e) => setForm({ ...form, slug: e.target.value })} />
            </div>
          </div>
          <div className="field">
            <label>描述（選填）</label>
            <input className="input" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>
          <div className="field">
            <label>父分類（空白＝根分類）</label>
            <select className="select" value={form.parentId} onChange={(e) => setForm({ ...form, parentId: e.target.value })}>
              <option value="">（無，根分類）</option>
              {flat.filter((c) => editingId == null || !blockedIds.has(c.id)).map((c) => (
                <option key={c.id} value={c.id}>{'\u00A0'.repeat(c.depth * 2)}{c.name}</option>
              ))}
            </select>
          </div>
          <div className="actions">
            <button type="submit" className="btn btn-primary">{editingId == null ? '新增' : '儲存變更'}</button>
            {editingId != null && (
              <button type="button" className="btn" onClick={() => { setEditingId(null); setForm({ name: '', slug: '', description: '', parentId: '' }); }}>
                取消編輯
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="card">
        <h3>分類樹</h3>
        {tree.length === 0 && <p className="muted">尚無分類。</p>}
        {renderTree(tree || [])}
      </div>

      {deleting && (
        <div className="modal-backdrop" onClick={() => setDeleting(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>刪除分類「{deleting.name}」</h3>
            <p className="muted">
              {deleting.articleCount > 0
                ? '此分類下仍有文章。後端規定刪除前必須把文章移轉到其他分類：'
                : '此分類沒有文章，可直接刪除。'}
            </p>
            {deleting.articleCount > 0 && (
              <div className="field">
                <label>移轉文章到 *</label>
                <select className="select" value={moveTo} onChange={(e) => setMoveTo(e.target.value)}>
                  <option value="">請選擇目標分類</option>
                  {flat.filter((c) => c.id !== deleting.id).map((c) => (
                    <option key={c.id} value={c.id}>{'\u00A0'.repeat(c.depth * 2)}{c.name}</option>
                  ))}
                </select>
              </div>
            )}
            <div className="actions">
              <button
                className="btn btn-danger"
                disabled={deleting.articleCount > 0 && !moveTo}
                onClick={submitDelete}
              >
                確認刪除
              </button>
              <button className="btn" onClick={() => setDeleting(null)}>取消</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}