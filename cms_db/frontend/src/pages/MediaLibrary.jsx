// 媒體庫（後端 §11）
// 上傳（author+）/ 列表（admin、editor 看全部，author 看自己）/ 更新 altText / 刪除
import { useEffect, useRef, useState } from 'react';
import * as mediaApi from '../api/media.js';
import Pagination from '../components/Pagination.jsx';
import Alert from '../components/Alert.jsx';
import { formatDate } from '../utils.js';

export default function MediaLibrary() {
  const [data, setData] = useState(null);
  const [page, setPage] = useState(0);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [uploading, setUploading] = useState(false);
  const fileRef = useRef();

  const load = (p) => mediaApi.list({ page: p, size: 12 }).then(setData).catch((e) => setError(e.message));

  useEffect(() => {
    load(page);
  }, [page]);

  const onFile = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setError(null);
    setSuccess(null);
    setUploading(true);
    try {
      const m = await mediaApi.upload(file);
      setSuccess(`已上傳「${m.fileName}」（${(m.fileSize / 1024).toFixed(1)} KB）`);
      setPage(0);
      load(0);
    } catch (err) {
      setError(err.message);
    } finally {
      setUploading(false);
      if (fileRef.current) fileRef.current.value = '';
    }
  };

  const saveAlt = async (m, value) => {
    try {
      await mediaApi.updateAltText(m.id, value);
    } catch (err) {
      setError(err.message);
    }
  };

  const remove = async (m) => {
    if (!window.confirm(`確定刪除「${m.fileName}」？`)) return;
    try {
      await mediaApi.remove(m.id);
      load(page);
    } catch (err) {
      setError(err.message); // 例如非上傳者的 editor → 後端回 403
    }
  };

  return (
    <div>
      <div className="page-title">
        <h1>媒體庫</h1>
        <div>
          <input ref={fileRef} type="file" style={{ display: 'none' }} onChange={onFile} />
          <button className="btn btn-success" disabled={uploading} onClick={() => fileRef.current?.click()}>
            {uploading ? '上傳中…' : '＋ 上傳檔案'}
          </button>
        </div>
      </div>

      {error && <Alert variant="error">{error}</Alert>}
      {success && <Alert variant="success">{success}</Alert>}

      <div className="media-grid">
        {data?.content?.map((m) => (
          <div key={m.id} className="media-card">
            {m.fileType.startsWith('image/') ? (
              <img src={mediaApi.mediaUrl(m.url)} alt={m.altText || m.fileName} />
            ) : (
              <div className="media-fallback">📄 {m.fileName}</div>
            )}
            <div className="media-body">
              <div className="muted" style={{ wordBreak: 'break-all' }}>{m.fileName}</div>
              <div className="muted">{(m.fileSize / 1024).toFixed(1)} KB ｜ {m.uploaderName} ｜ {formatDate(m.createdAt)}</div>
              <div style={{ marginTop: 6 }}>
                <input
                  className="input"
                  style={{ padding: 4, fontSize: 12 }}
                  placeholder="altText（描述）"
                  defaultValue={m.altText || ''}
                  onBlur={(e) => e.target.value !== (m.altText || '') && saveAlt(m, e.target.value)}
                />
                <div className="muted" style={{ fontSize: 12, marginTop: 4 }}>可直接使用網址：{mediaApi.mediaUrl(m.url)}</div>
              </div>
              <div style={{ marginTop: 8 }}>
                <button className="btn btn-sm btn-danger" onClick={() => remove(m)}>刪除</button>
              </div>
            </div>
          </div>
        ))}
      </div>

      <Pagination page={data?.page ?? 0} totalPages={data?.totalPages ?? 0} totalElements={data?.totalElements} onChange={setPage} />
    </div>
  );
}