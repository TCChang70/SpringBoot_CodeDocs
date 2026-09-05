import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { getMyResults } from '../../api/examApi'

const GRADE_COLOR = { A:'#065f46', B:'#1e40af', C:'#92400e', D:'#9a3412', F:'#991b1b' }

export default function MyResultsPage() {
  const { auth } = useAuth()
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getMyResults(auth.token)
      .then(setResults)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [auth.token])

  if (loading) return <div className="loading">⏳ 載入成績中...</div>
  if (error) return <div className="alert alert-error">{error}</div>

  const visible = results.filter(r => !r.scoreHidden)
  const avg = visible.length
    ? (visible.reduce((s, r) => s + r.percentage, 0) / visible.length).toFixed(1)
    : 0
  const best = visible.length ? Math.max(...visible.map(r => r.percentage)) : 0
  const hiddenCount = results.length - visible.length

  return (
    <>
      <div className="page-header">
        <h1 className="page-title">我的成績</h1>
      </div>

      {results.length === 0 ? (
        <div className="empty">
          <div className="empty-icon">📊</div>
          <p>尚未參加任何測驗</p>
        </div>
      ) : (
        <>
          <div className="stats-row">
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--primary)' }}>{results.length}</div>
              <div className="stat-label">已完成測驗</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: '#7c3aed' }}>{avg}%</div>
              <div className="stat-label">平均得分率</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--success)' }}>{best}%</div>
              <div className="stat-label">最高得分率</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: '#065f46' }}>
                {results.filter(r => r.grade === 'A').length}
              </div>
              <div className="stat-label">A 級成績</div>
            </div>
          </div>

          {hiddenCount > 0 && (
            <div className="alert alert-info">
              🔒 共有 {hiddenCount} 筆成績尚未公布，公布後即可在下方查看。
            </div>
          )}

          <div className="card">
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>測驗名稱</th>
                    <th>次數</th>
                    <th>得分</th>
                    <th>得分率</th>
                    <th>等級</th>
                    <th>提交時間</th>
                  </tr>
                </thead>
                <tbody>
                  {results.map(r => (
                    <tr key={r.id}>
                      <td style={{ fontWeight: 500 }}>{r.examTitle}</td>
                      <td className="text-sm">
                        {r.attemptNumber != null
                          ? <span style={{ background:'#d1fae5', color:'#065f46', padding:'.15rem .5rem', borderRadius:999, fontSize:'.8rem', fontWeight:500 }}>第 {r.attemptNumber} 次</span>
                          : <span className="text-muted text-sm">—</span>}
                      </td>
                      {r.scoreHidden ? (
                        <td colSpan={3}>
                          <span className="text-muted text-sm" style={{ fontWeight: 500 }}>
                            🔒 成績尚未公布
                          </span>
                        </td>
                      ) : (
                        <>
                          <td>{r.score} / {r.totalPoints}</td>
                          <td>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '.5rem' }}>
                              <div className="progress-bar-bg" style={{ flex: 1, height: 8 }}>
                                <div
                                  className="progress-bar-fill"
                                  style={{
                                    width: `${r.percentage}%`,
                                    background: r.percentage >= 80 ? 'var(--success)' : r.percentage >= 60 ? 'var(--warning)' : 'var(--danger)',
                                    height: 8,
                                  }}
                                />
                              </div>
                              <span style={{ minWidth: 42, textAlign: 'right' }}>{r.percentage}%</span>
                            </div>
                          </td>
                          <td>
                            <span
                              className={`badge badge-grade-${r.grade}`}
                              style={{ fontWeight: 700, fontSize: '.9rem', color: GRADE_COLOR[r.grade] }}
                            >
                              {r.grade}
                            </span>
                          </td>
                        </>
                      )}
                      <td className="text-sm text-muted">
                        {new Date(r.submittedAt).toLocaleString('zh-TW')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </>
  )
}
