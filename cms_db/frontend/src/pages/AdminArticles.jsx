// 文章管理台：全部文章 + 狀態篩選 + 審核動作（後端 §9.8）
// 動作：待審→核准/判退；已發佈→封存；已封存→重新發佈；一律可刪除
import { useEffect, useState } from 'react';
import * as articlesApi from '../api/articles.js';
import StatusBadge from '../components/StatusBadge.jsx';
import Pagination from '../components/Pagination.jsx';
import Alert from '../components/Alert.jsx';
import { formatDate } from '../utils.js';
import { ARTICLE_STATUS_LABELS } from '../constants.js';

const STATUS_OPTIONS = ['', ...Object.keys(ARTICLE_STATUS_LABELS)];

export default function AdminArticles() {
  const [status, setStatus] = useState('');
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  const load = (p, s, k) => {
    const params = { page: p, size: 10 };
    if (s) params.status = s;
    if (k.trim()) params.keyword = k.trim();
    articlesApi.adminList(params).then(setData).catch((e) => setError(e.message));
  };

  useEffect(() => {
    load(page, status, keyword);
  }, [page, status]);

  const act = async (fn, table) => {
    try {
      await fn(table.id);
      load(page, status, keyword);
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <div>
      <div className="page-title"><h1>文章管理</h1></div>

      <div className="toolbar">
        <select className="select" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
          <option value="">全部狀態</option>
          {STATUS_OPTIONS.filter(Boolean).map((s) => (
            <option key={s} value={s}>{ARTICLE_STATUS_LABELS[s]}</option>
          ))}
        </select>
        <input
          className="input"
          placeholder="關鍵字（標題/摘要）"
          defaultValue={keyword}
          onKeyDown={(e) => {
            if (e.key === 'Enter') { setKeyword(e.target.value); setPage(0); }
          }}
        />
      </div>

      {error && <Alert variant="error">{error}</Alert>}

      <div className="card">
        {data?.content?.length === 0 && <p className="muted">沒有符合條件的文章。</p>}
        <table className="table">
          <thead>
            <tr>
              <th>標題</th><th>作者</th><th>狀態</th><th>瀏覽</th><th>建立時間</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            {data?.content?.map((a) => (
              <tr key={a.id}>
                <td>
                  {a.title}
                  <div className="muted" style={{ fontSize: 12 }}>{a.categories.map((c) => <span key={c} className="chip">#{c}</span>)}</div>
                </td>
                <td>{a.authorName}</td>
                <td><StatusBadge status={a.status} /></td>
                <td>{a.viewCount}</td>
                <td className="muted">{formatDate(a.createdAt)}</td>
                <td>
                  <div className="actions">
                    {a.status === 'pending_review' && (
                      <>
                        <button className="btn btn-sm btn-success" onClick={() => act(articlesApi.approve, a)}>核准</button>
                        <button className="btn btn-sm btn-warn" onClick={() => act(articlesApi.reject, a)}>判退</button>
                      </>
                    )}
                    {a.status === 'published' && (
                      <button className="btn btn-sm btn-warn" onClick={() => act(articlesApi.archive, a)}>封存</button>
                    )}
                    {a.status === 'archived' && (
                      <button className="btn btn-sm btn-success" onClick={() => act(articlesApi.publish, a)}>重新發佈</button>
                    )}
                    <button
                      className="btn btn-sm btn-danger"
                      onClick={() => window.confirm(`確定刪除「${a.title}」？`) && act(articlesApi.remove, a)}
                    >
                      刪除
                    </button>
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