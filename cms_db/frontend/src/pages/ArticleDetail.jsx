// 前台文章詳情 + 留言（後端 §9.4、§10）
// 瀏覽此頁會讓後端 viewCount +1
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import * as articlesApi from '../api/articles.js';
import * as commentsApi from '../api/comments.js';
import Alert from '../components/Alert.jsx';
import { formatDate } from '../utils.js';
import { useAuth } from '../context/AuthContext.jsx';

function CommentNode({ comment, onReply }) {
  const [replies] = useState(comment.children || []);
  return (
    <div className="comment">
      <div className="comment-head">
        <strong>{comment.authorName}</strong>
        {comment.userId != null && <span className="chip">會員</span>}
        <span>{formatDate(comment.createdAt)}</span>
      </div>
      <p style={{ margin: '4px 0 8px' }}>{comment.content}</p>
      <button className="btn btn-sm" onClick={() => onReply(comment)}>回覆</button>
      {replies.length > 0 && (
        <div className="comment-children">
          {replies.map((c) => (
            <CommentNode key={c.id} comment={c} onReply={onReply} />
          ))}
        </div>
      )}
    </div>
  );
}

export default function ArticleDetail() {
  const { id } = useParams();
  const { isLoggedIn } = useAuth();

  const [article, setArticle] = useState(null);
  const [notFound, setNotFound] = useState(false);
  const [comments, setComments] = useState([]);

  const [form, setForm] = useState({ content: '', authorName: '', authorEmail: '', parentId: null });
  const [replyingTo, setReplyingTo] = useState(null);
  const [msg, setMsg] = useState(null);
  const [err, setErr] = useState(null);
  const [sending, setSending] = useState(false);

  useEffect(() => {
    articlesApi
      .detail(id)
      .then(setArticle)
      .catch((e) => {
        if (e.status === 404) setNotFound(true);
        else setErr(e.message);
      });
    commentsApi.list(id).then(setComments).catch(() => {});
  }, [id]);

  const startReply = (comment) => {
    setReplyingTo(comment);
    setForm((f) => ({ ...f, parentId: comment.id }));
    window.scrollTo({ top: document.getElementById('comment-form')?.offsetTop - 90, behavior: 'smooth' });
  };

  const submitComment = async (e) => {
    e.preventDefault();
    setErr(null);
    setMsg(null);
    const payload = { content: form.content, parentId: form.parentId || null };
    if (!isLoggedIn) {
      payload.authorName = form.authorName;
      payload.authorEmail = form.authorEmail || null;
    }
    try {
      setSending(true);
      await commentsApi.create(id, payload);
      setForm({ content: '', authorName: '', authorEmail: '', parentId: null });
      setReplyingTo(null);
      setMsg('留言已送出，待管理員審核後才會公開。');
      // 重新載入已核准留言（若有被立即核准）
      commentsApi.list(id).then(setComments).catch(() => {});
    } catch (err) {
      setErr(err.message);
    } finally {
      setSending(false);
    }
  };

  if (notFound) {
    return <div className="card"><Alert variant="error">文章不存在或尚未發佈。</Alert></div>;
  }
  if (!article) return <p className="muted">載入中…</p>;

  return (
    <div className="article-detail">
      <div className="card">
        <h1>{article.title}</h1>
        <div className="meta">
          <span>作者：{article.authorName}</span>
          <span>瀏覽：{article.viewCount}</span>
          <span>發佈：{formatDate(article.publishedAt)}</span>
        </div>
        {article.featuredImage && (
          <img src={article.featuredImage} alt="" style={{ maxWidth: '100%', borderRadius: 8, margin: '12px 0' }} />
        )}
        <div className="article-content">{article.content}</div>
      </div>

      <div className="card">
        <h3>留言（{(comments || []).length}）</h3>
        {(comments || []).length === 0 && <p className="muted">尚無留言。</p>}
        {(comments || []).map((c) => (
          <CommentNode key={c.id} comment={c} onReply={startReply} />
        ))}

        <div id="comment-form" style={{ marginTop: 20 }}>
          <h3>{replyingTo ? `回覆 ${replyingTo.authorName}` : '留言'}</h3>
          {!isLoggedIn && (
            <div className="form-row">
              <div className="field">
                <label>暱稱</label>
                <input
                  className="input"
                  value={form.authorName}
                  onChange={(e) => setForm({ ...form, authorName: e.target.value })}
                />
              </div>
              <div className="field">
                <label>信箱（選填）</label>
                <input
                  className="input"
                  value={form.authorEmail}
                  onChange={(e) => setForm({ ...form, authorEmail: e.target.value })}
                />
              </div>
            </div>
          )}
          {isLoggedIn && <p className="muted">以會員身份留言（將顯示你的帳號名稱）。</p>}
          <form onSubmit={submitComment}>
            <div className="field">
              <textarea
                className="textarea"
                style={{ minHeight: 90 }}
                placeholder="留言內容（最多 2000 字）"
                value={form.content}
                onChange={(e) => setForm({ ...form, content: e.target.value })}
              />
            </div>
            {replyingTo && (
              <button type="button" className="btn btn-sm" onClick={() => { setReplyingTo(null); setForm({ ...form, parentId: null }); }}>
                取消回覆
              </button>
            )}
            <button type="submit" className="btn btn-primary" disabled={sending || !form.content.trim()}>
              {sending ? '送出中…' : '送出留言'}
            </button>
          </form>
          {err && <Alert variant="error">{err}</Alert>}
          {msg && <Alert variant="success">{msg}</Alert>}
        </div>
      </div>
    </div>
  );
}