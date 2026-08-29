import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { getExamForStudent, submitExam } from '../../api/examApi'

const GRADE_COLOR = { A: '#065f46', B: '#1e40af', C: '#92400e', D: '#9a3412', F: '#991b1b' }

function formatTime(secs) {
  const m = Math.floor(secs / 60).toString().padStart(2, '0')
  const s = (secs % 60).toString().padStart(2, '0')
  return `${m}:${s}`
}

export default function TakeExamPage() {
  const { id } = useParams()
  const { auth } = useAuth()
  const navigate = useNavigate()

  const [exam, setExam] = useState(null)
  const [answers, setAnswers] = useState({})    // { "questionId": "A" }
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [timeLeft, setTimeLeft] = useState(null)

  useEffect(() => {
    getExamForStudent(auth.token, id)
      .then(data => { setExam(data); setTimeLeft(data.timeLimit * 60) })
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [auth.token, id])

  useEffect(() => {
    if (!timeLeft || timeLeft <= 0 || result) return
    const t = setTimeout(() => setTimeLeft(s => s - 1), 1000)
    return () => clearTimeout(t)
  }, [timeLeft, result])

  async function handleSubmit() {
    const unanswered = (exam?.questions?.length ?? 0) - Object.keys(answers).length
    if (unanswered > 0 && !window.confirm(`尚有 ${unanswered} 題未作答，確定要提交嗎？`)) return
    setSubmitting(true)
    setError('')
    try {
      const data = await submitExam(auth.token, id, answers)
      setResult(data)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <div className="loading">⏳ 載入題目中...</div>

  if (error && !exam) return (
    <div>
      <div className="alert alert-error">{error}</div>
      <button className="btn btn-ghost" onClick={() => navigate('/student')}>← 返回測驗列表</button>
    </div>
  )

  /* ── Result Screen ── */
  if (result) {
    return (
      <div className="card" style={{ maxWidth: 520, margin: '2rem auto', textAlign: 'center' }}>
        <div style={{ fontSize: '3.5rem', marginBottom: '.75rem' }}>
          {result.grade === 'A' ? '🏆' : result.grade === 'B' ? '🎉' : result.grade === 'C' ? '👍' : '📚'}
        </div>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '.25rem' }}>測驗完成！</h2>
        <p className="text-muted text-sm" style={{ marginBottom: '1.5rem' }}>{result.examTitle}</p>

        <div className="result-big-score">{result.score} / {result.totalPoints}</div>
        <div className="result-grade" style={{ color: GRADE_COLOR[result.grade] ?? '#374151' }}>
          {result.grade}
        </div>
        <p className="text-muted" style={{ marginTop: '.5rem' }}>{result.percentage}%</p>

        <div
          style={{
            background: '#f9fafb', borderRadius: '.5rem', padding: '.875rem',
            margin: '1.25rem 0', fontSize: '.875rem', color: '#4b5563'
          }}
        >
          {result.percentage >= 90 ? '🌟 優秀！繼續保持！'
            : result.percentage >= 70 ? '💪 不錯，再接再厲！'
            : '📖 建議複習相關章節後再次挑戰！'}
        </div>

        <div style={{ display: 'flex', gap: '.75rem', justifyContent: 'center' }}>
          <button className="btn btn-primary" onClick={() => navigate('/student')}>
            返回測驗列表
          </button>
          <button className="btn btn-ghost" onClick={() => navigate('/student/results')}>
            查看所有成績
          </button>
        </div>
      </div>
    )
  }

  /* ── Exam Screen ── */
  const answered = Object.keys(answers).length
  const total = exam.questions.length
  const isWarning = timeLeft !== null && timeLeft < 300

  return (
    <>
      {/* Sticky progress bar + controls */}
      <div style={{
        position: 'sticky', top: 56, background: 'white',
        borderBottom: '1px solid #e5e7eb', padding: '.75rem 0',
        marginBottom: '1.5rem', zIndex: 50,
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '.5rem' }}>
          <div>
            <span style={{ fontWeight: 700 }}>{exam.title}</span>
            <span className="text-muted text-sm" style={{ marginLeft: '.75rem' }}>
              已作答 {answered} / {total} 題
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            {timeLeft !== null && (
              <span style={{ fontWeight: 700, fontSize: '1.1rem', color: isWarning ? '#ef4444' : '#374151' }}>
                ⏱ {formatTime(timeLeft)}
                {isWarning && <span style={{ fontSize: '.75rem', marginLeft: '.4rem' }}>⚠ 時間不多了</span>}
              </span>
            )}
            <button className="btn btn-success" onClick={handleSubmit} disabled={submitting}>
              {submitting ? '提交中...' : '提交作答'}
            </button>
          </div>
        </div>
        {/* Progress bar */}
        <div className="progress-bar-bg" style={{ height: 6 }}>
          <div className="progress-bar-fill" style={{ width: `${(answered / total) * 100}%`, background: '#3b82f6' }} />
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {/* Questions */}
      {exam.questions.map((q, idx) => {
        const selected = answers[String(q.id)]
        return (
          <div key={q.id} className="question-card">
            <div className="question-number">第 {idx + 1} 題 · {q.points} 分</div>
            <div className="question-text">{q.questionText}</div>
            <div className="options">
              {['A', 'B', 'C', 'D'].map(opt => (
                <label
                  key={opt}
                  className={`option${selected === opt ? ' selected' : ''}`}
                  onClick={() => setAnswers(prev => ({ ...prev, [String(q.id)]: opt }))}
                >
                  <span className="option-label">{opt}</span>
                  <span>{q[`option${opt}`]}</span>
                </label>
              ))}
            </div>
          </div>
        )
      })}

      <div style={{ textAlign: 'center', padding: '1rem 0 2rem' }}>
        <button
          className="btn btn-success btn-lg"
          onClick={handleSubmit}
          disabled={submitting}
        >
          {submitting ? '提交中...' : `✅ 提交作答（已作答 ${answered}/${total} 題）`}
        </button>
      </div>
    </>
  )
}
