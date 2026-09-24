// 待審文章工作列（後端 §9.8 /pending）— 快速核准 / 判退
import { useEffect, useState } from 'react';
import * as articlesApi from '../api/articles.js';
import Alert from '../components/Alert.jsx';
import { formatDate } from '../utils.js';

export default function Moderation() {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  const load = () => {
    articlesApi.pendingList({ page: 0, size: 20 }).then(setData).catch((e) => setError(e.message));
  };

  useEffect(() => { load(); }, []);

  const act = async (fn, a) => {
    try {
      await fn(a.id);
      load();
    } catch (e) {
      setError(e.message);
    }
  };

  return (
    <div>
      <div className="page-title"><h1>待審文章</h1></div>
      {error && <Alert variant="error">{error}</Alert>}

      <div className="card">
        {data?.content?.length === 0 && <p className="muted">沒有待審核的文章。</p>}
        {data?.content?.map((a) => (
          <div key={a.id} className="article-list-item">
            <h3 style={{ margin: 0 }}>{a.title}</h3>
            <p className="muted">{a.summary || '（無摘要）'}</p>
            <div className="meta">
              <span>作者：{a.authorName}</span>
              <span>建立：{formatDate(a.createdAt)}</span>
            </div>
            <div className="actions" style={{ marginTop: 8 }}>
              <button className="btn btn-sm btn-success" onClick={() => act(articlesApi.approve, a)}>核准發佈</button>
              <button className="btn btn-sm btn-warn" onClick={() => act(articlesApi.reject, a)}>退回草稿</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}