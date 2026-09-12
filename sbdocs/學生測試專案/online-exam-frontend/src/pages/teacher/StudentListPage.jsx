import { useState, useEffect, useCallback, useMemo } from 'react'
import { useAuth } from '../../context/AuthContext'
import { getStudents, getStudentClasses, createStudent, updateStudent, deleteStudent, batchImportStudents } from '../../api/examApi'

const EMPTY_CREATE = { username: '', password: '', displayName: '', className: '' }
const EMPTY_EDIT   = { displayName: '', className: '', newPassword: '' }

const IMPORT_HEADERS = ['帳號', '姓名', '班級', '密碼']
const TEMPLATE_URL = '/examples/student-import-template.csv'

function splitCsvLine(line) {
  const cells = []
  let cur = ''
  let inQuote = false
  for (let i = 0; i < line.length; i++) {
    const ch = line[i]
    if (inQuote) {
      if (ch === '"') {
        if (line[i + 1] === '"') { cur += '"'; i++ } else inQuote = false
      } else cur += ch
    } else if (ch === '"') {
      inQuote = true
    } else if (ch === ',') {
      cells.push(cur); cur = ''
    } else {
      cur += ch
    }
  }
  cells.push(cur)
  return cells
}

function parseStudentImport(text) {
  const rows = []
  const clean = (text || '').replace(/^\uFEFF/, '')
  clean.split(/\r?\n/).forEach((line, idx) => {
    const cells = splitCsvLine(line.trim())
    if (!cells.length || cells.every(c => !c.trim())) return
    if (idx === 0 && cells.some(c => IMPORT_HEADERS.includes(c.trim()))) return
    const [username = '', displayName = '', className = '', password = ''] = cells.map(c => c.trim())
    if (!username) return
    rows.push({ username, displayName, className, password, line: idx + 1 })
  })
  return rows
}

export default function StudentListPage() {
  const { auth } = useAuth()
  const [students, setStudents] = useState([])
  const [classes, setClasses]   = useState([])
  const [filter, setFilter]     = useState('')
  const [loading, setLoading]   = useState(true)
  const [error, setError]       = useState('')
  const [showAdd, setShowAdd]   = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [createForm, setCreateForm] = useState(EMPTY_CREATE)
  const [editForm, setEditForm]     = useState(EMPTY_EDIT)
  const [saving, setSaving] = useState(false)
  const [showImport, setShowImport] = useState(false)
  const [importText, setImportText] = useState('')
  const [importing, setImporting] = useState(false)
  const [importResult, setImportResult] = useState(null)
  const [importError, setImportError] = useState('')

  const reload = useCallback(() => {
    setLoading(true)
    Promise.all([
      getStudents(auth.token, filter || undefined),
      getStudentClasses(auth.token),
    ]).then(([s, c]) => { setStudents(s); setClasses(c) })
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [auth.token, filter])

  useEffect(() => { reload() }, [reload])

  async function handleCreate(e) {
    e.preventDefault(); setSaving(true); setError('')
    try {
      await createStudent(auth.token, createForm)
      setShowAdd(false); setCreateForm(EMPTY_CREATE); reload()
    } catch (err) { setError(err.message) }
    finally { setSaving(false) }
  }

  async function handleUpdate(e) {
    e.preventDefault(); setSaving(true); setError('')
    try {
      await updateStudent(auth.token, editingId, editForm)
      setEditingId(null); reload()
    } catch (err) { setError(err.message) }
    finally { setSaving(false) }
  }

  async function handleDelete(s) {
    if (!window.confirm(`確定刪除「${s.displayName}」（${s.username}）？\n此操作也將刪除其所有考試記錄，且無法復原。`)) return
    setError('')
    try { await deleteStudent(auth.token, s.id); reload() }
    catch (err) { setError(err.message) }
  }

  function startEdit(s) {
    setEditingId(s.id)
    setEditForm({ displayName: s.displayName, className: s.className ?? '', newPassword: '' })
    setShowAdd(false)
  }

  function handleImportFile(e) {
    const file = e.target.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = () => { setImportText(String(reader.result ?? '')); setImportResult(null); setImportError('') }
    reader.readAsText(file, 'UTF-8')
    e.target.value = ''
  }

  async function handleBatchImport() {
    setImporting(true); setImportError(''); setImportResult(null)
    try {
      const payload = importRows.map(({ username, displayName, className, password }) => ({ username, displayName, className, password }))
      const res = await batchImportStudents(auth.token, payload)
      setImportResult(res)
      setImportText('')
      reload()
    } catch (err) { setImportError(err.message) }
    finally { setImporting(false) }
  }

  const importRows = useMemo(() => parseStudentImport(importText), [importText])

  const importIssues = useMemo(() => {
    const seen = new Set()
    return importRows.map(r => {
      const issues = []
      if (!r.username) issues.push('缺帳號')
      else if (r.username.length < 3) issues.push('帳號過短(>=3)')
      if (!r.displayName) issues.push('缺姓名')
      if (!r.password) issues.push('缺密碼')
      else if (r.password.length < 6) issues.push('密碼過短(>=6)')
      if (r.username) {
        const key = r.username
        if (seen.has(key)) issues.push('檔內重複')
        seen.add(key)
      }
      return issues
    })
  }, [importRows])

  const filtered = filter ? students.filter(s => s.className === filter) : students
  const classCounts = students.reduce((acc, s) => {
    const k = s.className || '（未設定）'; acc[k] = (acc[k] ?? 0) + 1; return acc
  }, {})

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">學生管理</h1>
          <p className="text-muted text-sm">共 {students.length} 位學生 · {classes.length} 個班級</p>
        </div>
        <div style={{ display: 'flex', gap: '.6rem' }}>
          <button className="btn btn-ghost"
            onClick={() => { setShowImport(v => !v); setShowAdd(false) }}>
            {showImport ? '✕ 取消' : '⬆ 批次匯入'}
          </button>
          <button className="btn btn-teacher"
            onClick={() => { setShowAdd(v => !v); setEditingId(null); setShowImport(false) }}>
            {showAdd ? '✕ 取消' : '＋ 新增學生'}
          </button>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {/* ── Batch import form ── */}
      {showImport && (
        <div className="card" style={{ border: '2px solid var(--teacher)', marginBottom: '1rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '.5rem' }}>
            <h3 style={{ fontWeight: 700, color: 'var(--teacher)' }}>⬆ 批次匯入學生帳號</h3>
            <a className="btn btn-ghost btn-sm" href={TEMPLATE_URL} download="學生匯入範例.csv">⬇ 下載匯入範例檔</a>
          </div>
          <p className="text-muted text-sm" style={{ marginBottom: '1rem' }}>
            每一列一位學生，欄位順序：<strong>帳號, 姓名, 班級, 密碼</strong>（首列為標題列，可省略）。
            可上傳 CSV 檔，或直接將內容貼到下方欄位。
          </p>
          <input type="file" accept=".csv,.txt,text/csv,.xlsx" className="form-input" style={{ maxWidth: 360, marginBottom: '.75rem' }} onChange={handleImportFile} />
          <textarea
            className="form-input"
            rows={6}
            placeholder={`範例：\n帳號,姓名,班級,密碼\ns109001,王小明,資工一甲,password123\ns109002,陳小美,資工一甲,password123`}
            value={importText}
            onChange={e => { setImportText(e.target.value); setImportResult(null); setImportError('') }}
            style={{ fontFamily: 'monospace', marginBottom: '.75rem' }}
          />
          {importRows.length > 0 && (
            <div className="table-wrapper" style={{ marginBottom: '1rem' }}>
              <table>
                <thead>
                  <tr><th>行</th><th>帳號</th><th>姓名</th><th>班級</th><th>密碼</th><th>檢查</th></tr>
                </thead>
                <tbody>
                  {importRows.map((r, i) => (
                    <tr key={i}>
                      <td className="text-muted text-sm">{r.line}</td>
                      <td>{r.username || '—'}</td>
                      <td>{r.displayName || '—'}</td>
                      <td>{r.className || '—'}</td>
                      <td>{r.password ? '••••••' : '—'}</td>
                      <td>
                        {importIssues[i].length
                          ? <span style={{ color: '#ef4444', fontSize: '.8rem' }}>{importIssues[i].join('、')}</span>
                          : <span style={{ color: '#166534', fontSize: '.8rem' }}>✓</span>}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {importError && <div className="alert alert-error" style={{ marginBottom: '.75rem' }}>{importError}</div>}
          {importResult && (
            <div className={`alert ${importResult.duplicates.length ? 'alert-info' : 'alert-success'}`} style={{ marginBottom: '.75rem' }}>
              成功匯入 <strong>{importResult.imported}</strong> 位學生
              {importResult.duplicates.length > 0 && (
                <>，跳過重複（已存在或檔內重複）{importResult.duplicates.length} 筆：{importResult.duplicates.join('、')}</>
              )}
            </div>
          )}
          <div style={{ display: 'flex', gap: '.75rem', justifyContent: 'flex-end' }}>
            <button type="button" className="btn btn-ghost"
              onClick={() => { setShowImport(false); setImportResult(null); setImportError(''); setImportText('') }}>
              關閉
            </button>
            <button type="button" className="btn btn-teacher"
              disabled={importing || importRows.length === 0} onClick={handleBatchImport}>
              {importing ? '匯入中...' : `開始匯入（${importRows.length} 位）`}
            </button>
          </div>
        </div>
      )}

      {/* ── Create form ── */}
      {showAdd && (
        <div className="card" style={{ border: '2px solid var(--teacher)', marginBottom: '1rem' }}>
          <h3 style={{ fontWeight: 700, color: 'var(--teacher)', marginBottom: '1rem' }}>➕ 新增學生帳號</h3>
          <form onSubmit={handleCreate}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(200px,1fr))', gap: '1rem' }}>
              <div className="form-group">
                <label className="form-label">帳號 *</label>
                <input className="form-input" value={createForm.username}
                  onChange={e => setCreateForm({ ...createForm, username: e.target.value })}
                  placeholder="3–50 字元" required minLength={3} autoFocus />
              </div>
              <div className="form-group">
                <label className="form-label">顯示姓名 *</label>
                <input className="form-input" value={createForm.displayName}
                  onChange={e => setCreateForm({ ...createForm, displayName: e.target.value })}
                  placeholder="學生姓名" required />
              </div>
              <div className="form-group">
                <label className="form-label">班級 *</label>
                <input className="form-input" list="cls-opts" value={createForm.className}
                  onChange={e => setCreateForm({ ...createForm, className: e.target.value })}
                  placeholder="例如：資工三甲" required />
                <datalist id="cls-opts">{classes.map(c => <option key={c} value={c} />)}</datalist>
              </div>
              <div className="form-group">
                <label className="form-label">初始密碼 *</label>
                <input type="password" className="form-input" value={createForm.password}
                  onChange={e => setCreateForm({ ...createForm, password: e.target.value })}
                  placeholder="至少 6 個字元" required minLength={6} />
              </div>
            </div>
            <div style={{ display: 'flex', gap: '.75rem', justifyContent: 'flex-end' }}>
              <button type="button" className="btn btn-ghost" onClick={() => setShowAdd(false)}>取消</button>
              <button type="submit" className="btn btn-teacher" disabled={saving}>
                {saving ? '建立中...' : '建立帳號'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* ── Stats per class ── */}
      {!filter && classes.length > 0 && (
        <div className="stats-row">
          {Object.entries(classCounts).map(([cls, cnt]) => (
            <div key={cls} className="stat-card"
              style={{ cursor: 'pointer', borderLeft: '3px solid var(--teacher)' }}
              onClick={() => setFilter(cls === '（未設定）' ? '' : cls)}>
              <div className="stat-value" style={{ color: 'var(--teacher)', fontSize: '1.5rem' }}>{cnt}</div>
              <div className="stat-label">{cls}</div>
            </div>
          ))}
        </div>
      )}

      {/* ── Class filter tabs ── */}
      <div style={{ display: 'flex', gap: '.5rem', flexWrap: 'wrap', marginBottom: '1rem', alignItems: 'center' }}>
        <span className="text-sm" style={{ fontWeight: 500, color: 'var(--gray-700)' }}>篩選班級：</span>
        <button className={`btn btn-sm ${!filter ? 'btn-teacher' : 'btn-ghost'}`} onClick={() => setFilter('')}>
          全部（{students.length}）
        </button>
        {classes.map(cls => (
          <button key={cls} className={`btn btn-sm ${filter === cls ? 'btn-teacher' : 'btn-ghost'}`}
            onClick={() => setFilter(cls)}>
            {cls}（{students.filter(s => s.className === cls).length}）
          </button>
        ))}
      </div>

      {/* ── Student table ── */}
      {loading ? (
        <div className="loading">⏳ 載入中...</div>
      ) : filtered.length === 0 ? (
        <div className="empty"><div className="empty-icon">👥</div><p>尚無學生資料</p></div>
      ) : (
        <div className="card">
          <div className="table-wrapper">
            <table>
              <thead>
                <tr><th>#</th><th>姓名</th><th>帳號</th><th>班級</th><th>操作</th></tr>
              </thead>
              <tbody>
                {filtered.map((s, idx) =>
                  editingId === s.id ? (
                    <tr key={s.id} style={{ background: '#f5f3ff' }}>
                      <td colSpan={5}>
                        <form onSubmit={handleUpdate}
                          style={{ display: 'flex', gap: '.75rem', alignItems: 'flex-end', padding: '.375rem 0', flexWrap: 'wrap' }}>
                          <div className="form-group" style={{ margin: 0, flex: '1 1 140px' }}>
                            <label className="form-label">姓名</label>
                            <input className="form-input" value={editForm.displayName}
                              onChange={e => setEditForm({ ...editForm, displayName: e.target.value })} required />
                          </div>
                          <div className="form-group" style={{ margin: 0, flex: '1 1 140px' }}>
                            <label className="form-label">班級</label>
                            <input className="form-input" list="cls-opts-edit" value={editForm.className}
                              onChange={e => setEditForm({ ...editForm, className: e.target.value })} />
                            <datalist id="cls-opts-edit">{classes.map(c => <option key={c} value={c} />)}</datalist>
                          </div>
                          <div className="form-group" style={{ margin: 0, flex: '1 1 180px' }}>
                            <label className="form-label">新密碼（留空不變）</label>
                            <input type="password" className="form-input" value={editForm.newPassword}
                              onChange={e => setEditForm({ ...editForm, newPassword: e.target.value })}
                              placeholder="留空 = 不修改" minLength={6} />
                          </div>
                          <div style={{ display: 'flex', gap: '.4rem', paddingBottom: '1px' }}>
                            <button type="submit" className="btn btn-teacher btn-sm" disabled={saving}>
                              {saving ? '...' : '✅ 儲存'}
                            </button>
                            <button type="button" className="btn btn-ghost btn-sm"
                              onClick={() => setEditingId(null)}>取消</button>
                          </div>
                        </form>
                      </td>
                    </tr>
                  ) : (
                    <tr key={s.id}>
                      <td className="text-muted text-sm">{idx + 1}</td>
                      <td style={{ fontWeight: 500 }}>{s.displayName}</td>
                      <td className="text-muted">{s.username}</td>
                      <td>
                        {s.className
                          ? <span style={{ background: '#dbeafe', color: '#1e40af', padding: '.15rem .55rem', borderRadius: 999, fontSize: '.8rem', fontWeight: 500 }}>{s.className}</span>
                          : <span className="text-muted text-sm">—</span>}
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: '.4rem' }}>
                          <button className="btn btn-ghost btn-sm" onClick={() => startEdit(s)}>✏️ 編輯</button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleDelete(s)}>🗑 刪除</button>
                        </div>
                      </td>
                    </tr>
                  )
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </>
  )
}
