// 前台文章列表（後端 §9.3）
// 支援：關鍵字、分類、標籤篩選 + 伺服器分頁
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import * as articlesApi from '../api/articles.js';
import * as categoriesApi from '../api/categories.js';
import * as tagsApi from '../api/tags.js';
import Pagination from '../components/Pagination.jsx';
import Alert from '../components/Alert.jsx';
import { formatDate } from '../utils.js';
import { useAuth } from '../context/AuthContext.jsx';

const SIZE = 10;

export default function Home() {
  const { isLoggedIn } = useAuth();
  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [tagId, setTagId] = useState('');
  const [page, setPage] = useState(0);

  const [data, setData] = useState(null);
  const [categories, setCategories] = useState([]);
  const [tags, setTags] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    categoriesApi.tree().then(setCategories).catch(() => {});
    tagsApi.list().then(setTags).catch(() => {});
  }, []);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    const params = {
      page,
      size: SIZE,
      sort: 'publishedAt,desc',
    };
    if (keyword.trim()) params.keyword = keyword.trim();
    if (categoryId) params.categoryId = categoryId;
    if (tagId) params.tagId = tagId;

    articlesApi
      .list(params)
      .then((d) => !cancelled && setData(d))
      .catch((e) => !cancelled && setError(e.message))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, [page, keyword, categoryId, tagId]);

  const applyFilters = (reset = true) => {
    if (reset) setPage(0);
    setKeyword(document.getElementById('kw')?.value ?? '');
    setCategoryId(document.getElementById('cat')?.value ?? '');
    setTagId(document.getElementById('tag')?.value ?? '');
  };

  return (
    <div>
      <div className="toolbar">
        <input id="kw" className="input" placeholder="搜尋標題 / 摘要" defaultValue={keyword} />
        <select id="cat" className="select" defaultValue={categoryId}>
          <option value="">全部分類</option>
          {(categories || []).map((c) => (
            <option key={c.id} value={c.id}>{c.name}</option>
          ))}
        </select>
        <select id="tag" className="select" defaultValue={tagId}>
          <option value="">全部標籤</option>
          {(tags || []).map((t) => (
            <option key={t.id} value={t.id}>{t.name}</option>
          ))}
        </select>
        <button className="btn btn-primary" onClick={() => applyFilters()}>查詢</button>
        {isLoggedIn && (
          <Link to="/editor/new" className="btn btn-success" style={{ marginLeft: 'auto' }}>＋ 寫新文章</Link>
        )}
      </div>

      {error && <Alert variant="error">{error}</Alert>}
      {loading && <p className="muted">載入中…</p>}

      <div className="card">
        {data?.content?.length === 0 && <p className="muted">沒有符合條件的文章。</p>}
        {data?.content?.map((a) => (
          <div key={a.id} className="article-list-item">
            <h3><Link to={`/articles/${a.id}`}>{a.title}</Link></h3>
            <p className="muted">{a.summary || '（無摘要）'}</p>
            <div className="meta">
              <span>作者：{a.authorName}</span>
              <span>瀏覽：{a.viewCount}</span>
              <span>發佈：{formatDate(a.publishedAt)}</span>
              {a.categories.map((c) => (
                <span key={c} className="chip">#{c}</span>
              ))}
              {a.tags.map((t) => (
                <span key={t} className="chip">@{t}</span>
              ))}
            </div>
          </div>
        ))}
      </div>

      <Pagination page={data?.page ?? 0} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements} onChange={setPage} />
    </div>
  );
}