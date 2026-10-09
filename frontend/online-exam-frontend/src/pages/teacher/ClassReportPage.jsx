import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { getStudentClasses, getClassReport } from '../../api/examApi'

const GRADE_COLOR = { A:'#065f46', B:'#1e40af', C:'#92400e', D:'#9a3412', F:'#991b1b' }

function round1(v) { return Math.round(v * 10) / 10 }

export default function ClassReportPage() {
  const { auth } = useAuth()
  const [classes, setClasses] = useState([])
  const [className, setClassName] = useState('')
  const [examId, setExamId] = useState('')
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getStudentClasses(auth.token)
      .then(cs => {
        setClasses(cs)
        if (cs.length === 0) setLoading(false)
        else setClassName(cs[0])
      })
      .catch(err => { setError(err.message); setLoading(false) })
  }, [auth.token])

  useEffect(() => {
    if (!className) return
    setLoading(true)
    setError('')
    setExamId('')
    getClassReport(auth.token, className)
      .then(setReport)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [auth.token, className])

  const selectedExam = report?.exams.find(e => String(e.examId) === String(examId)) ?? null

  const ranked = selectedExam
    ? report.students
        .map(s => ({ ...s, cell: s.cells.find(c => String(c.examId) === String(selectedExam.examId)) }))
        .filter(x => x.cell && x.cell.percentage != null)
        .sort((a, b) => b.cell.percentage - a.cell.percentage)
    : []

  const examPcts = ranked.map(x => x.cell.percentage)
  const examAvg = examPcts.length ? round1(examPcts.reduce((a, b) => a + b, 0) / examPcts.length) : null
  const examHigh = examPcts.length ? round1(Math.max(...examPcts)) : null
  const examLow = examPcts.length ? round1(Math.min(...examPcts)) : null

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">班級成績報表</h1>
          <p className="text-muted text-sm">
            以班級為單位彙總各測驗成績（僅含您建立的測驗）
            {selectedExam ? ` · 科目：${selectedExam.examTitle}` : ''}
          </p>
        </div>
        <button className="btn btn-teacher no-print" onClick={() => window.print()}
          disabled={!report || report.students.length === 0}>
          🖨 列印 / 存成 PDF
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {classes.length > 0 && (
        <div className="no-print" style={{ display: 'flex', gap: '.5rem', flexWrap: 'wrap', marginBottom: '.75rem', alignItems: 'center' }}>
          <span className="text-sm" style={{ fontWeight: 500, color: 'var(--gray-700)' }}>選擇班級：</span>
          {classes.map(cls => (
            <button key={cls} className={`btn btn-sm ${className === cls ? 'btn-teacher' : 'btn-ghost'}`}
              onClick={() => setClassName(cls)}>
              {cls}
            </button>
          ))}
        </div>
      )}

      {report && report.exams.length > 0 && (
        <div className="no-print" style={{ display: 'flex', gap: '.5rem', flexWrap: 'wrap', marginBottom: '1rem', alignItems: 'center' }}>
          <span className="text-sm" style={{ fontWeight: 500, color: 'var(--gray-700)' }}>選擇科目：</span>
          <button className={`btn btn-sm ${!examId ? 'btn-teacher' : 'btn-ghost'}`} onClick={() => setExamId('')}>
            全部（成績矩陣）
          </button>
          {report.exams.map(e => (
            <button key={e.examId} className={`btn btn-sm ${String(examId) === String(e.examId) ? 'btn-teacher' : 'btn-ghost'}`}
              onClick={() => setExamId(e.examId)}>
              {e.examTitle}
            </button>
          ))}
        </div>
      )}

      {loading ? (
        <div className="loading">⏳ 載入班級成績報表中...</div>
      ) : classes.length === 0 ? (
        <div className="empty"><div className="empty-icon">🏫</div><p>尚無學生班級資料</p></div>
      ) : !report || report.students.length === 0 ? (
        <div className="empty"><div className="empty-icon">📊</div><p>此班級尚無學生</p></div>
      ) : report.exams.length === 0 ? (
        <div className="empty"><div className="empty-icon">📝</div><p>您尚未建立任何測驗</p></div>
      ) : selectedExam ? (
        <>
          <div className="stats-row">
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--teacher)' }}>{ranked.length}</div>
              <div className="stat-label">到考人數</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--primary)' }}>{examAvg ?? '—'}%</div>
              <div className="stat-label">該科平均</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--success)' }}>{examHigh ?? '—'}%</div>
              <div className="stat-label">最高分</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--danger)' }}>{examLow ?? '—'}%</div>
              <div className="stat-label">最低分</div>
            </div>
          </div>

          <div className="card">
            <h3 className="card-title">{report.className} · {selectedExam.examTitle} 成績報表</h3>
            {ranked.length === 0 ? (
              <div className="empty"><div className="empty-icon">📊</div><p>此科目尚無學生作答</p></div>
            ) : (
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>排名</th>
                      <th>班級</th>
                      <th>學生姓名</th>
                      <th>得分</th>
                      <th>得分率</th>
                      <th>等級</th>
                      <th>作答次數</th>
                      <th>最後提交時間</th>
                    </tr>
                  </thead>
                  <tbody>
                    {ranked.map((x, idx) => (
                      <tr key={x.studentId}>
                        <td style={{ color: idx < 3 ? ['#f59e0b','#9ca3af','#b45309'][idx] : '#d1d5db', fontWeight: 700 }}>
                          #{idx + 1}
                        </td>
                        <td>
                          {x.studentClass
                            ? <span style={{ background:'#dbeafe', color:'#1e40af', padding:'.15rem .5rem', borderRadius:999, fontSize:'.8rem', fontWeight:500 }}>{x.studentClass}</span>
                            : <span className="text-muted text-sm">—</span>}
                        </td>
                        <td style={{ fontWeight: 500 }}>{x.studentName}</td>
                        <td>{x.cell.score} / {x.cell.totalPoints}</td>
                        <td>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '.5rem' }}>
                            <div className="progress-bar-bg" style={{ flex: 1, height: 8, minWidth: 60 }}>
                              <div className="progress-bar-fill" style={{ width: `${x.cell.percentage}%`, background: 'var(--teacher)', height: 8 }} />
                            </div>
                            <span style={{ minWidth: 42, textAlign: 'right' }}>{x.cell.percentage}%</span>
                          </div>
                        </td>
                        <td>
                          <span className={`badge badge-grade-${x.cell.grade}`} style={{ fontWeight: 700, color: GRADE_COLOR[x.cell.grade] }}>
                            {x.cell.grade}
                          </span>
                        </td>
                        <td className="text-sm">{x.cell.attemptCount} 次</td>
                        <td className="text-sm text-muted">
                          {x.cell.lastSubmittedAt ? new Date(x.cell.lastSubmittedAt).toLocaleString('zh-TW') : '—'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          <p className="report-footer print-only text-muted text-sm">
            列印日期：{new Date().toLocaleString('zh-TW')}
          </p>
        </>
      ) : (
        <>
          <div className="stats-row">
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--teacher)' }}>{report.students.length}</div>
              <div className="stat-label">班級人數</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--primary)' }}>{report.exams.length}</div>
              <div className="stat-label">測驗數</div>
            </div>
            <div className="stat-card">
              <div className="stat-value" style={{ color: 'var(--success)' }}>
                {report.exams.reduce((s, e) => s + e.participantCount, 0)}
              </div>
              <div className="stat-label">作答總人次</div>
            </div>
          </div>

          <div className="card">
            <h3 className="card-title">{report.className} · 成績矩陣</h3>
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>#</th>
                    <th>學生姓名</th>
                    {report.exams.map(e => (
                      <th key={e.examId} style={{ textAlign: 'center' }}>{e.examTitle}</th>
                    ))}
                    <th style={{ textAlign: 'center' }}>個人平均</th>
                  </tr>
                </thead>
                <tbody>
                  {report.students.map((s, idx) => (
                    <tr key={s.studentId}>
                      <td className="text-muted text-sm">{idx + 1}</td>
                      <td style={{ fontWeight: 500 }}>{s.studentName}</td>
                      {report.exams.map((e, i) => {
                        const cell = s.cells[i]
                        return (
                          <td key={e.examId} style={{ textAlign: 'center' }}>
                            {cell && cell.percentage != null ? (
                              <span style={{ fontWeight: 600, color: GRADE_COLOR[cell.grade] }}>
                                {cell.percentage}%
                                <span className="text-sm text-muted" style={{ marginLeft: '.35rem', fontWeight: 400 }}>
                                  {cell.grade}
                                </span>
                              </span>
                            ) : (
                              <span className="text-muted text-sm">—</span>
                            )}
                          </td>
                        )
                      })}
                      <td style={{ textAlign: 'center', fontWeight: 700 }}>
                        {s.averagePercentage != null ? `${s.averagePercentage}%` : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr style={{ background: 'var(--gray-50)' }}>
                    <td />
                    <td style={{ fontWeight: 600 }}>班級平均</td>
                    {report.exams.map(e => (
                      <td key={e.examId} style={{ textAlign: 'center', fontWeight: 600 }}>
                        {e.averagePercentage != null ? `${e.averagePercentage}%` : '—'}
                        <div className="text-sm text-muted" style={{ fontWeight: 400 }}>
                          {e.participantCount} 人作答
                        </div>
                      </td>
                    ))}
                    <td />
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>

          <p className="report-footer print-only text-muted text-sm">
            列印日期：{new Date().toLocaleString('zh-TW')}
          </p>
        </>
      )}
    </>
  )
}
