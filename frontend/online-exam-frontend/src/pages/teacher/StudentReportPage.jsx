import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { getStudentReport } from '../../api/examApi'
import StudentReportCard from '../../components/StudentReportCard'
import { reportExamOptions, filterStudentReport } from '../../utils/reportFilter'

export default function StudentReportPage() {
  const { id } = useParams()
  const { auth } = useAuth()
  const navigate = useNavigate()
  const [report, setReport] = useState(null)
  const [examId, setExamId] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getStudentReport(auth.token, id)
      .then(setReport)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [auth.token, id])

  if (loading) return <div className="loading">⏳ 載入學生成績報表中...</div>
  if (error) return <div className="alert alert-error">{error}</div>

  const options = reportExamOptions(report)
  const display = filterStudentReport(report, examId)

  return (
    <>
      <div className="page-header">
        <div>
          <button className="btn btn-ghost btn-sm no-print" onClick={() => navigate('/teacher/students')}>
            ← 返回學生管理
          </button>
          <h1 className="page-title" style={{ marginTop: '.5rem' }}>學生成績報表</h1>
          <p className="text-muted text-sm">
            {report.studentName}
            {report.studentClass ? ` · ${report.studentClass}` : ''} ·
            {examId ? ' 單一科目成績' : ' 僅含您建立的測驗'}
          </p>
        </div>
        <button className="btn btn-teacher no-print" onClick={() => window.print()}>
          🖨 列印 / 存成 PDF
        </button>
      </div>

      {options.length > 1 && (
        <div className="no-print" style={{ display: 'flex', gap: '.5rem', flexWrap: 'wrap', marginBottom: '1rem', alignItems: 'center' }}>
          <span className="text-sm" style={{ fontWeight: 500, color: 'var(--gray-700)' }}>選擇科目：</span>
          <button className={`btn btn-sm ${!examId ? 'btn-teacher' : 'btn-ghost'}`} onClick={() => setExamId('')}>
            全部科目
          </button>
          {options.map(o => (
            <button key={o.examId} className={`btn btn-sm ${String(examId) === String(o.examId) ? 'btn-teacher' : 'btn-ghost'}`}
              onClick={() => setExamId(o.examId)}>
              {o.examTitle}
            </button>
          ))}
        </div>
      )}

      <StudentReportCard report={display} />

      <p className="report-footer print-only text-muted text-sm">
        列印日期：{new Date().toLocaleString('zh-TW')}
      </p>
    </>
  )
}
