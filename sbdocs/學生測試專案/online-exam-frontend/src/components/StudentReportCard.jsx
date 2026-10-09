const GRADE_BG = { A:'#10b981', B:'#3b82f6', C:'#f59e0b', D:'#f97316', F:'#ef4444' }
const GRADE_COLOR = { A:'#065f46', B:'#1e40af', C:'#92400e', D:'#9a3412', F:'#991b1b' }

export default function StudentReportCard({ report }) {
  if (!report) return null

  const dist = report.gradeDistribution ?? {}
  const distTotal = Object.values(dist).reduce((s, n) => s + n, 0)
  const visibleCount = report.items.filter(i => !i.scoreHidden).length

  return (
    <>
      <div className="stats-row">
        <div className="stat-card">
          <div className="stat-value" style={{ color: 'var(--primary)' }}>{report.examCount}</div>
          <div className="stat-label">測驗數（共 {report.totalAttempts} 次作答）</div>
        </div>
        <div className="stat-card">
          <div className="stat-value" style={{ color: 'var(--teacher)' }}>{report.averagePercentage}%</div>
          <div className="stat-label">平均得分率</div>
        </div>
        <div className="stat-card">
          <div className="stat-value" style={{ color: 'var(--success)' }}>{report.bestPercentage}%</div>
          <div className="stat-label">最高得分率</div>
        </div>
        <div className="stat-card">
          <div className="stat-value" style={{ color: 'var(--danger)' }}>{report.lowestPercentage}%</div>
          <div className="stat-label">最低得分率</div>
        </div>
      </div>

      {visibleCount === 0 ? (
        <div className="alert alert-info">🔒 目前沒有可顯示的成績（可能尚未參加測驗或成績尚未公布）。</div>
      ) : (
        <div className="card" style={{ marginBottom: '1rem' }}>
          <h3 className="card-title">等級分布</h3>
          {['A', 'B', 'C', 'D', 'F'].map(grade => {
            const count = dist[grade] ?? 0
            const pct = distTotal ? (count / distTotal * 100) : 0
            return (
              <div key={grade} style={{ display: 'flex', alignItems: 'center', gap: '.75rem', marginBottom: '.5rem' }}>
                <span className={`badge badge-grade-${grade}`} style={{ width: '2rem', justifyContent: 'center', fontWeight: 700 }}>
                  {grade}
                </span>
                <div className="progress-bar-bg" style={{ flex: 1, height: 22, borderRadius: '.375rem' }}>
                  <div style={{
                    width: `${pct}%`, background: GRADE_BG[grade], height: 22, borderRadius: '.375rem',
                    transition: 'width .4s', display: 'flex', alignItems: 'center', paddingLeft: '.5rem',
                    color: 'white', fontSize: '.75rem', fontWeight: 600,
                  }}>
                    {count > 0 && `${count} 科`}
                  </div>
                </div>
                <span className="text-sm text-muted" style={{ minWidth: 40 }}>{pct.toFixed(0)}%</span>
              </div>
            )
          })}
        </div>
      )}

      <div className="card">
        <h3 className="card-title">逐測驗成績</h3>
        {report.items.length === 0 ? (
          <div className="empty"><div className="empty-icon">📊</div><p>尚無作答紀錄</p></div>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>測驗名稱</th>
                  <th>作答次數</th>
                  <th>得分</th>
                  <th>得分率</th>
                  <th>等級</th>
                  <th>最後提交時間</th>
                </tr>
              </thead>
              <tbody>
                {report.items.map((it, idx) => (
                  <tr key={it.examId}>
                    <td className="text-muted text-sm">{idx + 1}</td>
                    <td style={{ fontWeight: 500 }}>{it.examTitle}</td>
                    <td className="text-sm">
                      {it.attemptCount > 0
                        ? <span style={{ background:'#d1fae5', color:'#065f46', padding:'.15rem .5rem', borderRadius:999, fontSize:'.8rem', fontWeight:500 }}>{it.attemptCount} 次</span>
                        : <span className="text-muted text-sm">—</span>}
                    </td>
                    {it.scoreHidden ? (
                      <td colSpan={3}>
                        <span className="text-muted text-sm" style={{ fontWeight: 500 }}>🔒 成績尚未公布</span>
                      </td>
                    ) : (
                      <>
                        <td>{it.score} / {it.totalPoints}</td>
                        <td>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '.5rem' }}>
                            <div className="progress-bar-bg" style={{ flex: 1, height: 8, minWidth: 60 }}>
                              <div className="progress-bar-fill" style={{
                                width: `${it.percentage}%`,
                                background: it.percentage >= 80 ? 'var(--success)' : it.percentage >= 60 ? 'var(--warning)' : 'var(--danger)',
                                height: 8,
                              }} />
                            </div>
                            <span style={{ minWidth: 42, textAlign: 'right' }}>{it.percentage}%</span>
                          </div>
                        </td>
                        <td>
                          <span className={`badge badge-grade-${it.grade}`}
                            style={{ fontWeight: 700, fontSize: '.9rem', color: GRADE_COLOR[it.grade] }}>
                            {it.grade}
                          </span>
                        </td>
                      </>
                    )}
                    <td className="text-sm text-muted">
                      {it.lastSubmittedAt ? new Date(it.lastSubmittedAt).toLocaleString('zh-TW') : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  )
}
