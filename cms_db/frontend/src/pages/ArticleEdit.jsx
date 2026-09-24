// 文章編輯器：新增 / 編輯（後端 §9.2、§9.6）
// - slug 依標題自動產生，可手動覆寫；與他人/自己重複時後端回 40902
// - 編輯時後端回傳的分類/標籤是「名稱」，先由 name→id 反向對應回勾選狀態
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as articlesApi from '../api/articles.js';
import * as categoriesApi from '../api/categories.js';
import * as tagsApi from '../api/tags.js';
import Alert from '../components/Alert.jsx';
import { slugify } from '../utils.js';

export default function ArticleEdit() {
  const { id } = useParams();
  const isEdit = !!id;
  const navigate = useNavigate();

  const [categories, setCategories] = useState([]);
  const [tags, setTags] = useState([]);
  const [form, setForm] = useState({
    title: '', slug: '', summary: '', content: '', featuredImage: '',
    categoryIds: [], tagIds: [],
  });
  const [slugTouched, setSlugTouched] = useState(false);
  const [loading, setLoading] = useState(isEdit);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    Promise.all([
      categoriesApi.tree(),
      tagsApi.list(),
      isEdit ? articlesApi.getManage(id) : Promise.resolve(null),
    ])
      .then(([tree, tagList, article]) => {
        const flatCats = categoriesApi.flattenTree(tree || []);
        setCategories(flatCats);
        setTags(tagList || []);

        if (article) {
          // 名稱 → id 反向對應
          const catIdByName = Object.fromEntries(flatCats.map((c) => [c.name, c.id]));
          const tagIdByName = Object.fromEntries((tagList || []).map((t) => [t.name, t.id]));
          setForm({
            title: article.title || '',
            slug: article.slug || '',
            summary: article.summary || '',
            content: article.content || '',
            featuredImage: article.featuredImage || '',
            categoryIds: (article.categories || []).map((n) => catIdByName[n]).filter(Boolean),
            tagIds: (article.tags || []).map((n) => tagIdByName[n]).filter(Boolean),
          });
        }
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id, isEdit]);

  const toggle = (list, value) => (list.includes(value) ? list.filter((v) => v !== value) : [...list, value]);

  // 標題變更時自動產生 slug（使用者尚未手動改過）
  const onTitleChange = (value) => {
    setForm((f) => ({
      ...f,
      title: value,
      slug: slugTouched ? f.slug : slugify(value),
    }));
  };

  const save = async (e) => {
    e.preventDefault();
    setError(null);
    if (!form.slug.trim()) {
      setError('slug 不可為空');
      return;
    }
    setBusy(true);
    const payload = {
      title: form.title,
      slug: form.slug,
      summary: form.summary,
      content: form.content,
      featuredImage: form.featuredImage || null,
      categoryIds: [...form.categoryIds],
      tagIds: [...form.tagIds],
    };
    try {
      if (isEdit) {
        await articlesApi.update(id, payload);
      } else {
        await articlesApi.create(payload);
      }
      navigate('/my');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const previewCount = useMemo(() => form.content.length, [form.content]);

  return (
    <div>
      <div className="page-title"><h1>{isEdit ? '編輯文章' : '寫新文章'}</h1></div>
      {error && <Alert variant="error">{error}</Alert>}
      {loading && <p className="muted">載入中…</p>}

      {!loading && (
        <form onSubmit={save} className="card">
          <div className="field">
            <label>標題 *</label>
            <input className="input" value={form.title} onChange={(e) => onTitleChange(e.target.value)} maxLength={255} />
          </div>
          <div className="field">
            <label>slug *（網址用，全站唯一）</label>
            <input
              className="input"
              value={form.slug}
              onChange={(e) => { setSlugTouched(true); setForm({ ...form, slug: e.target.value }); }}
            />
            <div className="hint">依標題自動產生；若與既有文章重複，後端會回錯誤（40902）。</div>
          </div>
          <div className="field">
            <label>摘要（選填，≤500）</label>
            <input className="input" value={form.summary} onChange={(e) => setForm({ ...form, summary: e.target.value })} maxLength={500} />
          </div>
          <div className="field">
            <label>內文 *</label>
            <textarea className="textarea" value={form.content} onChange={(e) => setForm({ ...form, content: e.target.value })} />
            <div className="hint">目前 {previewCount} 字元。</div>
          </div>
          <div className="field">
            <label>封面圖網址（選填）</label>
            <input className="input" value={form.featuredImage} onChange={(e) => setForm({ ...form, featuredImage: e.target.value })} placeholder="https://... 或後端媒體庫回傳的 url" />
          </div>

          <div className="field">
            <label>分類（可複選）</label>
            <div className="checkbox-group">
              {(categories || []).map((c) => (
                <label key={c.id}>
                  <input
                    type="checkbox"
                    checked={form.categoryIds.includes(c.id)}
                    onChange={() => setForm({ ...form, categoryIds: toggle(form.categoryIds, c.id) })}
                  />
                  {'\u00A0'.repeat(c.depth * 2)}{c.name}
                </label>
              ))}
            </div>
          </div>

          <div className="field">
            <label>標籤（可複選）</label>
            <div className="checkbox-group">
              {(tags || []).map((t) => (
                <label key={t.id}>
                  <input
                    type="checkbox"
                    checked={form.tagIds.includes(t.id)}
                    onChange={() => setForm({ ...form, tagIds: toggle(form.tagIds, t.id) })}
                  />
                  {t.name}
                </label>
              ))}
            </div>
          </div>

          <button type="submit" className="btn btn-primary" disabled={busy || !form.title.trim() || !form.content.trim()}>
            {busy ? '儲存中…' : '儲存'}
          </button>
          <button type="button" className="btn" style={{ marginLeft: 8 }} onClick={() => navigate('/my')}>取消</button>
        </form>
      )}
    </div>
  );
}