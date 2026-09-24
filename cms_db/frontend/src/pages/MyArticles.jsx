// 作者的「我的文章」（後端 §9.8 /mine）
// 動作依狀態：草稿→編輯/送審/刪除；待審→編輯/刪除；已發佈→刪除(作者可刪)；已封存→需管理員
import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import * as articlesApi from '../api/articles.js';
import StatusBadge from '../components/StatusBadge.jsx';
import Pagination from '../components/Pagination.jsx';
import Alert from '../components/Alert.jsx';
import { formatDate } from '../utils.js';
import { ARTICLE_STATUS_LABELS } from '../constants.js';

const STATUS_OPTIONS = ['', 'draft', 'pending_review', 'published', 'archived'];

export default function MyArticles() {
  const navigate = useNavigate();
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const load = (p, s) => {
    setError(null);
    articlesApi
      .mine(s ? { status: s, page: p, size: 10 } : { page: p, size: 10 })
      .then(setData)
      .catch((e) => setError(e.message));
  };

  useEffect(() => {
    load(page, status);
  }, [page, status]);

  const act = async (fn, id) => {
    setBusy(true);
    try {
      await fn(id);
      load(page, status);
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div>
      <div className="page-title">
        <h1>我的文章</h1>
        <Link to="/editor/new" className="btn btn-success">＋ 寫新文章</Link>
      </div>

      <div className="toolbar">
        <select className="select" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
          <option value="">全部狀態</option>
          {STATUS_OPTIONS.filter(Boolean).map((s) => (
            <option key={s} value={s}>{ARTICLE_STATUS_LABELS[s]}</option>
          ))}
        </select>
      </div>

      {error && <Alert variant="error">{error}</Alert>}

      <div className="card">
        {data?.content?.length === 0 && <p className="muted">尚無文章。點右上角「寫新文章」開始。</p>}
        <table className="table">
          <thead>
            <tr>
              <th>標題</th><th>狀態</th><th>瀏覽</th><th>建立時間</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            {data?.content?.map((a) => (
              <tr key={a.id}>
                <td>
                  {a.title}
                  <div className="muted" style={{ fontSize: 12 }}>
                    {a.categories.map((c) => <span key={c} className="chip">#{c}</span>)}
                    {' '}{a.tags.map((t) => <span key={t} className="chip">@{t}</span>)}
                  </div>
                </td>
                <td><StatusBadge status={a.status} /></td>
                <td>{a.viewCount}</td>
                <td className="muted">{formatDate(a.createdAt)}</td>
                <td>
                  <div className="actions">
                    {a.status !== 'archived' && (
                      <button className="btn btn-sm" onClick={() => navigate(`/editor/${a.id}`)}>編輯</button>
                    )}
                    {a.status === 'draft' && (
                      <button className="btn btn-sm btn-primary" disabled={busy} onClick={() => act(articlesApi.submit, a.id)}>送審</button>
                    )}
                    {a.status !== 'archived' && (
                      <button
                        className="btn btn-sm btn-danger"
                        disabled={busy}
                        onClick={() => window.confirm(`確定刪除「${a.title}」？`) && act(articlesApi.remove, a.id)}
                      >
                        刪除
                      </button>
                    )}
                    {a.status === 'archived' && <span className="muted">已封存，需管理員處理</span>}
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