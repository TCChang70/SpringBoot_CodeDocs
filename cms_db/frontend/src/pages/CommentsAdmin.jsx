// 留言審核（後端 §10.4）
// 動作：待審→核准/垃圾；已核准→垃圾/刪除；垃圾→復原/刪除
import { useEffect, useState } from 'react';
import * as commentsApi from '../api/comments.js';
import StatusBadge from '../components/StatusBadge.jsx';
import Pagination from '../components/Pagination.jsx';
import Alert from '../components/Alert.jsx';
import { formatDate } from '../utils.js';
import { COMMENT_STATUS_LABELS } from '../constants.js';

export default function CommentsAdmin() {
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  const load = (p, s) => {
    const params = { page: p, size: 10 };
    if (s) params.status = s;
    commentsApi.adminList(params).then(setData).catch((e) => setError(e.message));
  };

  useEffect(() => {
    load(page, status);
  }, [page, status]);

  const act = async (fn, c) => {
    try {
      await fn(c.id);
      load(page, status);
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <div>
      <div className="page-title"><h1>留言審核</h1></div>
      <div className="toolbar">
        <select className="select" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
          <option value="">全部狀態</option>
          {Object.keys(COMMENT_STATUS_LABELS).map((s) => (
            <option key={s} value={s}>{COMMENT_STATUS_LABELS[s]}</option>
          ))}
        </select>
      </div>

      {error && <Alert variant="error">{error}</Alert>}

      <div className="card">
        {data?.content?.length === 0 && <p className="muted">沒有符合條件的留言。</p>}
        <table className="table">
          <thead>
            <tr><th>留言者</th><th>內容</th><th>文章 ID</th><th>狀態</th><th>時間</th><th>操作</th></tr>
          </thead>
          <tbody>
            {data?.content?.map((c) => (
              <tr key={c.id}>
                <td>
                  {c.authorName}
                  <div className="muted" style={{ fontSize: 12 }}>
                    {c.userId != null ? `會員 #${c.userId}` : c.authorEmail}
                  </div>
                </td>
                <td style={{ maxWidth: 320, wordBreak: 'break-word' }}>{c.content}</td>
                <td>#{c.articleId}</td>
                <td><StatusBadge status={c.status} type="comment" /></td>
                <td className="muted">{formatDate(c.createdAt)}</td>
                <td>
                  <div className="actions">
                    {c.status === 'pending' && (
                      <>
                        <button className="btn btn-sm btn-success" onClick={() => act(commentsApi.approve, c)}>核准</button>
                        <button className="btn btn-sm btn-warn" onClick={() => act(commentsApi.markSpam, c)}>垃圾</button>
                      </>
                    )}
                    {c.status === 'approved' && (
                      <button className="btn btn-sm btn-warn" onClick={() => act(commentsApi.markSpam, c)}>垃圾</button>
                    )}
                    {c.status === 'spam' && (
                      <button className="btn btn-sm btn-success" onClick={() => act(commentsApi.restore, c)}>復原</button>
                    )}
                    <button className="btn btn-sm btn-danger" onClick={() => window.confirm('確定刪除這則留言？（回覆會一併刪除）') && act(commentsApi.remove, c)}>
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