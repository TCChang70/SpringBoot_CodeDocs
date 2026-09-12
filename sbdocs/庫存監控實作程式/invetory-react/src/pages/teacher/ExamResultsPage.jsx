import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { getExamResults, getExamResultDetail, getDeletedExamResults, deleteResult, restoreResult } from '../../api/examApi'
import OptionText from '../../components/OptionText'

const GRADE_BG = { A:'#10b981', B:'#3b82f6', C:'#f59e0b', D:'#f97316', F:'#ef4444' }

const LETTERS = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I']

export default function ExamResultsPage() {
  const { id } = useParams()
  const { auth } = useAuth()
  const navigate = useNavigate()
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [deleted, setDeleted] = useState([])
  const [detail, setDetail] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')

  useEffect(() => {
    const loadAll = async () => {
      setError('')
      try {
        const [active, d] = await Promise.all([
          getExamResults(auth.token, id),
          getDeletedExamResults(auth.token, id),
        ])
        setResults(active)
        setDeleted(d)
      } catch (err) {
        setError(err.message)
      } finally {
        setLoading(false)
      }
    }
    loadAll()
  }, [auth.token, id])

  function viewResult(r) {
    setDetailError('')
    setDetailLoading(true)
    setDetail(null)
    getExamResultDetail(auth.token, id, r.id)
      .then(setDetail)
      .catch(err => setDetailError(err.message))
      .finally(() => setDetailLoading(false))
  }

  async function handleDelete(r) {
    if (!window.confirm(`確定要刪除「${r.studentName}」的第 ${r.attemptNumber ?? '?'} 次作答紀錄嗎？\n刪除後該筆紀錄會隱藏（不會真正移除，可再復原）。`)) return
    setError('')
    try {
      await deleteResult(auth.token, r.id)
      setResults(list => list.filter(x => x.id !== r.id))
      getDeletedExamResults(auth.token, id).then(setDeleted).catch(() => {})
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleRestore(d) {
    setError('')
    try {
      await restoreResult(auth.token, d.id)
      setDeleted(list => list.filter(x => x.id !== d.id))
      getExamResults(auth.token, id).then(setResults).catch(() => {})
    } catch (err) {
      setError(err.message)
    }
  }

  if (loading) return <div className="loading">⏳ 載入成績中...</div>

  const examTitle = results[0]?.examTitle ?? '測驗'
  const gradeCount = results.reduce((acc, r) => { acc[r.grade] = (acc[r.grade] ?? 0) + 1; return acc }, {})
  const avg = results.length
    ? (results.reduce((s, r) => s + r.percentage, 0) / results.length).toFixed(1)
    : 0
  const highest = results.length ? Math.max(...results.map(r => r.percentage)) : 0
  const lowest  = results.length ? Math.min(...results.map(r => r.percentage)) : 0

  return (
    <>
      <div className="page-header">
        <div>
          <button className="btn btn-ghost btn-sm" onClick={() => navigate(`/teacher/exam/${id}`)}>
            ← 返回題目管理
          </button>
          <h1 className="page-title" style={{ marginTop: '.5rem' }}>{examTitle}</h1>
          <p className="text-muted text-sm">成績總覽</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {results.length === 0 ? (
        <div className="empty">
          <div className="empty-icon">📊</div>
          <p>尚無學生提交此測驗</p>
        </div>
      ) : (
        <>
          {/* Summary stats */}
          <div className="stats-row">
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--teacher)' }}>{results.length}</div>
              <div className="stat-label">已作答人數</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--primary)' }}>{avg}%</div>
              <div className="stat-label">班級平均</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--success)' }}>{highest}%</div>
              <div className="stat-label">最高分</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--danger)' }}>{lowest}%</div>
              <div className="stat-label">最低分</div>
            </div>
          </div>

          {/* Grade distribution */}
          <div className="card" style={{ marginBottom: '1rem' }}>
            <h3 className="card-title">等級分布</h3>
            {['A', 'B', 'C', 'D', 'F'].map(grade => {
              const count = gradeCount[grade] ?? 0
              const pct = results.length ? (count / results.length * 100) : 0
              return (
                <div key={grade} style={{ display: 'flex', alignItems: 'center', gap: '.75rem', marginBottom: '.5rem' }}>
                  <span className={`badge badge-grade-${grade}`} style={{ width: '2rem', justifyContent: 'center', fontWeight: 700 }}>
                    {grade}
                  </span>
                  <div className="progress-bar-bg" style={{ flex: 1, height: 22, borderRadius: '.375rem' }}>
                    <div style={{
                      width: `${pct}%`,
                      background: GRADE_BG[grade],
                      height: 22,
                      borderRadius: '.375rem',
                      transition: 'width .4s',
                      display: 'flex', alignItems: 'center', paddingLeft: '.5rem',
                      color: 'white', fontSize: '.75rem', fontWeight: 600,
                    }}>
                      {count > 0 && `${count} 人`}
                    </div>
                  </div>
                  <span className="text-sm text-muted" style={{ minWidth: 40 }}>
                    {pct.toFixed(0)}%
                  </span>
                </div>
              )
            })}
          </div>

          {/* Results table */}
          <div className="card">
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>排名</th>
                    <th>班級</th>
                    <th>學生姓名</th>
                    <th>次數</th>
                    <th>得分</th>
                    <th>得分率</th>
                    <th>等級</th>
                    <th>提交時間</th>
                    <th>作答紀錄</th>
                  </tr>
                </thead>
                <tbody>
                  {results.map((r, idx) => (
                    <tr key={r.id}>
                      <td style={{ color: idx < 3 ? ['#f59e0b','#9ca3af','#b45309'][idx] : '#d1d5db', fontWeight: 700 }}>
                        #{idx + 1}
                      </td>
                      <td>
                        {r.studentClass
                          ? <span style={{ background:'#dbeafe', color:'#1e40af', padding:'.15rem .5rem', borderRadius:999, fontSize:'.8rem', fontWeight:500 }}>{r.studentClass}</span>
                          : <span className="text-muted text-sm">—</span>
                        }
                      </td>
                      <td style={{ fontWeight: 500 }}>{r.studentName}</td>
                      <td className="text-sm">
                        {r.attemptNumber != null
                          ? <span style={{ background:'#d1fae5', color:'#065f46', padding:'.15rem .5rem', borderRadius:999, fontSize:'.8rem', fontWeight:500 }}>第 {r.attemptNumber} 次</span>
                          : <span className="text-muted text-sm">—</span>}
                      </td>
                      <td>{r.score} / {r.totalPoints}</td>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '.5rem' }}>
                          <div className="progress-bar-bg" style={{ flex: 1, height: 8, minWidth: 60 }}>
                            <div className="progress-bar-fill" style={{
                              width: `${r.percentage}%`,
                              background: 'var(--teacher)',
                              height: 8,
                            }} />
                          </div>
                          <span style={{ minWidth: 42, textAlign: 'right' }}>{r.percentage}%</span>
                        </div>
                      </td>
                      <td>
                        <span className={`badge badge-grade-${r.grade}`} style={{ fontWeight: 700 }}>
                          {r.grade}
                        </span>
                      </td>
                      <td className="text-sm text-muted">
                        {new Date(r.submittedAt).toLocaleString('zh-TW')}
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: '.4rem' }}>
                          <button className="btn btn-sm btn-teacher" onClick={() => viewResult(r)}>
                            📋 查看作答
                          </button>
                          <button className="btn btn-sm btn-danger" onClick={() => handleDelete(r)}>
                            🗑 刪除
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}

      {/* ── 已刪除紀錄（可復原） ── */}
      {deleted.length > 0 && (
        <div className="card" style={{ marginTop: '1rem', borderLeft: '4px solid var(--danger)' }}>
          <h3 className="card-title" style={{ color: 'var(--danger)' }}>
            🗑 已刪除的作答紀錄（{deleted.length}）
            <span className="text-sm text-muted" style={{ fontWeight: 400, marginLeft: '.5rem' }}>
              軟刪除：資料仍保留，僅從列表中隱藏，可隨時復原
            </span>
          </h3>
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>班級</th>
                  <th>學生姓名</th>
                  <th>次數</th>
                  <th>得分</th>
                  <th>提交時間</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                {deleted.map(d => (
                  <tr key={d.id} style={{ opacity: .85 }}>
                    <td>
                      {d.studentClass
                        ? <span style={{ background:'#dbeafe', color:'#1e40af', padding:'.15rem .5rem', borderRadius:999, fontSize:'.8rem', fontWeight:500 }}>{d.studentClass}</span>
                        : <span className="text-muted text-sm">—</span>}
                    </td>
                    <td style={{ fontWeight: 500 }}>{d.studentName}</td>
                    <td className="text-sm">第 {d.attemptNumber ?? '—'} 次</td>
                    <td>{d.score} / {d.totalPoints}</td>
                    <td className="text-sm text-muted">
                      {new Date(d.submittedAt).toLocaleString('zh-TW')}
                    </td>
                    <td>
                      <button className="btn btn-sm btn-teacher" onClick={() => handleRestore(d)}>
                        ↩️ 復原
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* ── 作答紀錄 modal ─────────────────────────── */}
      {(detail || detailLoading) && (
        <div className="modal-overlay" onMouseDown={e => { if (e.target === e.currentTarget) setDetail(null) }}>
          <div className="modal">
            <div className="modal-header">
              <h2 className="modal-title">作答紀錄</h2>
              <button className="btn btn-ghost btn-sm" onClick={() => setDetail(null)}>✕ 關閉</button>
            </div>

            {detailError && <div className="alert alert-error">{detailError}</div>}

            {detailLoading ? (
              <div className="loading">⏳ 載入作答紀錄中...</div>
            ) : detail && (
              <>
                <div className="answers-summary">
                  <div>
                    <div style={{ fontWeight: 700, fontSize: '1.05rem' }}>{detail.studentName}</div>
                    <div className="text-muted text-sm">
                      {detail.studentClass || '—'} · {new Date(detail.submittedAt).toLocaleString('zh-TW')}
                    </div>
                  </div>
                  <div className="answers-summary-stats">
                    <div className="answer-stat">
                      <span className="answer-stat-num" style={{ color: 'var(--primary)' }}>{detail.score}</span>
                      <span className="answer-stat-label">得分 / {detail.totalPoints}</span>
                    </div>
                    <div className="answer-stat">
                      <span className="answer-stat-num" style={{ color: detail.grade === 'F' ? 'var(--danger)' : GRADE_BG[detail.grade] }}>
                        {detail.grade}
                      </span>
                      <span className="answer-stat-label">等級</span>
                    </div>
                    <div className="answer-stat">
                      <span className="answer-stat-num" style={{ color: 'var(--success)' }}>{detail.correctCount}</span>
                      <span className="answer-stat-label">答對 / {detail.answers.length}</span>
                    </div>
                  </div>
                </div>

                <div className="answers-list">
                  {detail.answers.map((a, idx) => {
                    const correctParts = String(a.correctAnswer || '').split(',').map(s => s.trim().toUpperCase()).filter(Boolean)
                    const studentParts = String(a.studentAnswer || '').split(',').map(s => s.trim().toUpperCase()).filter(Boolean)
                    return (
                      <div key={a.questionId} className={`answer-item ${a.correct ? 'answer-item-correct' : 'answer-item-wrong'}`}>
                        <div className="answer-item-head">
                          <span className="answer-idx">題{idx + 1}</span>
                          <span className={`answer-badge ${a.correct ? 'answer-badge-ok' : 'answer-badge-no'}`}>
                            {a.correct ? '✓ 答對' : '✗ 答錯'}
                          </span>
                          {a.multiSelect && <span className="answer-multi">複選</span>}
                          {a.points != null && <span className="text-muted text-sm">{a.points} 分</span>}
                        </div>
                        <div className="question-text" style={{ marginBottom: '.5rem' }}>{a.questionText}</div>
                        <div className="answer-code-lines">
                          {LETTERS.filter(l => a[`option${l}`] != null && String(a[`option${l}`]).trim() !== '').map(l => {
                            const isCorrect = correctParts.includes(l)
                            const isStudent = studentParts.includes(l)
                            return (
                              <div key={l} className={`answer-option ${isCorrect ? 'answer-option-correct' : ''} ${(isStudent && !isCorrect) ? 'answer-option-wrong' : ''}`}>
                                <span className="answer-option-label">{l}</span>
                                <div className="answer-option-body">
                                  <OptionText value={a[`option${l}`]} />
                                  <div className="answer-option-chips">
                                    {isCorrect && <span className="chip chip-correct">正確答案</span>}
                                    {isStudent && isCorrect && <span className="chip chip-student-ok">學生選擇</span>}
                                    {(isStudent && !isCorrect) && <span className="chip chip-student-wrong">學生誤選</span>}
                                  </div>
                                </div>
                              </div>
                            )
                          })}
                        </div>
                        {studentParts.length === 0 && (
                          <div className="answer-unanswered">本題未作答</div>
                        )}
                      </div>
                    )
                  })}
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </>
  )
}
