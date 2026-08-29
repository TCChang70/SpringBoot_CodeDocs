import { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { getExamDetail, addQuestion, updateQuestion, deleteQuestion } from '../../api/examApi'

const EMPTY = {
  questionText: '', optionA: '', optionB: '', optionC: '', optionD: '',
  correctAnswer: 'A', points: 1,
}

function QuestionForm({ initial, onSave, onCancel, saving }) {
  const [form, setForm] = useState(initial)
  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  return (
    <div className="card" style={{ border: '2px solid var(--teacher)', marginBottom: '1rem' }}>
      <h3 style={{ fontWeight: 700, marginBottom: '1rem', color: 'var(--teacher)' }}>
        {initial.questionText ? '✏️ 編輯題目' : '➕ 新增題目'}
      </h3>
      <form onSubmit={e => { e.preventDefault(); onSave(form) }}>
        <div className="form-group">
          <label className="form-label">題目內容 *</label>
          <textarea className="form-textarea" value={form.questionText}
            onChange={e => set('questionText', e.target.value)}
            placeholder="輸入題目內容" required />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
          {['A', 'B', 'C', 'D'].map(opt => (
            <div key={opt} className="form-group">
              <label className="form-label">
                選項 {opt}
                {form.correctAnswer === opt && (
                  <span style={{ marginLeft: '.5rem', color: 'var(--success)', fontSize: '.8rem' }}>✓ 正確答案</span>
                )}
              </label>
              <input className="form-input" value={form[`option${opt}`]}
                onChange={e => set(`option${opt}`, e.target.value)}
                placeholder={`選項 ${opt}`} required />
            </div>
          ))}
        </div>

        <div style={{ display: 'flex', gap: '1rem' }}>
          <div className="form-group" style={{ flex: 1 }}>
            <label className="form-label">正確答案 *</label>
            <select className="form-select" value={form.correctAnswer}
              onChange={e => set('correctAnswer', e.target.value)}>
              {['A', 'B', 'C', 'D'].map(o => <option key={o}>{o}</option>)}
            </select>
          </div>
          <div className="form-group" style={{ flex: 1 }}>
            <label className="form-label">配分</label>
            <input type="number" className="form-input" value={form.points}
              onChange={e => set('points', Number(e.target.value))} min={1} />
          </div>
        </div>

        <div style={{ display: 'flex', gap: '.75rem', justifyContent: 'flex-end' }}>
          <button type="button" className="btn btn-ghost" onClick={onCancel}>取消</button>
          <button type="submit" className="btn btn-teacher" disabled={saving}>
            {saving ? '儲存中...' : (initial.questionText ? '更新題目' : '新增題目')}
          </button>
        </div>
      </form>
    </div>
  )
}

export default function ExamDetailPage() {
  const { id } = useParams()
  const { auth } = useAuth()
  const navigate = useNavigate()

  const [exam, setExam] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  const [showAddForm, setShowAddForm] = useState(false)
  const [editingId, setEditingId] = useState(null)

  const load = useCallback(() => {
    getExamDetail(auth.token, id)
      .then(setExam)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [auth.token, id])

  useEffect(load, [load])

  async function handleSave(form, qId) {
    setSaving(true)
    setError('')
    try {
      qId ? await updateQuestion(auth.token, qId, form) : await addQuestion(auth.token, id, form)
      setShowAddForm(false)
      setEditingId(null)
      load()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(qId) {
    if (!window.confirm('確定刪除此題目？')) return
    try {
      await deleteQuestion(auth.token, qId)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  if (loading) return <div className="loading">⏳ 載入中...</div>
  if (!exam) return <div className="alert alert-error">{error || '找不到測驗'}</div>

  const totalPoints = exam.questions.reduce((s, q) => s + q.points, 0)

  return (
    <>
      <div className="page-header">
        <div>
          <button className="btn btn-ghost btn-sm" onClick={() => navigate('/teacher')}>← 返回</button>
          <h1 className="page-title" style={{ marginTop: '.5rem' }}>{exam.title}</h1>
          <p className="text-muted text-sm" style={{ marginTop: '.25rem' }}>
            ⏱ {exam.timeLimit} 分鐘 · 📋 {exam.questions.length} 題 · 💯 共 {totalPoints} 分 ·&nbsp;
            <span className={`badge ${exam.active ? 'badge-active' : 'badge-inactive'}`}>
              {exam.active ? '開放中' : '已關閉'}
            </span>
          </p>
        </div>
        <div style={{ display: 'flex', gap: '.5rem', flexWrap: 'wrap' }}>
          <button className="btn btn-ghost btn-sm" onClick={() => navigate(`/teacher/exam/${id}/results`)}>
            📊 查看成績
          </button>
          <button className="btn btn-ghost btn-sm" onClick={() => navigate(`/teacher/exam/${id}/edit`)}>
            ✏️ 編輯測驗
          </button>
          <button className="btn btn-teacher" onClick={() => { setShowAddForm(true); setEditingId(null) }}>
            ＋ 新增題目
          </button>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {/* Add form at top */}
      {showAddForm && (
        <QuestionForm
          initial={EMPTY}
          onSave={form => handleSave(form, null)}
          onCancel={() => setShowAddForm(false)}
          saving={saving}
        />
      )}

      {exam.questions.length === 0 && !showAddForm ? (
        <div className="empty">
          <div className="empty-icon">❓</div>
          <p>尚未新增任何題目</p>
          <button className="btn btn-teacher" style={{ marginTop: '1rem' }}
            onClick={() => setShowAddForm(true)}>
            新增第一題
          </button>
        </div>
      ) : (
        exam.questions.map((q, idx) => {
          if (editingId === q.id) {
            return (
              <QuestionForm
                key={q.id}
                initial={{ questionText: q.questionText, optionA: q.optionA, optionB: q.optionB,
                           optionC: q.optionC, optionD: q.optionD, correctAnswer: q.correctAnswer, points: q.points }}
                onSave={form => handleSave(form, q.id)}
                onCancel={() => setEditingId(null)}
                saving={saving}
              />
            )
          }
          return (
            <div key={q.id} className="question-card" style={{ borderLeft: '4px solid var(--teacher)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '.5rem' }}>
                <div className="question-number">第 {idx + 1} 題 · {q.points} 分</div>
                <div style={{ display: 'flex', gap: '.4rem' }}>
                  <button className="btn btn-ghost btn-sm"
                    onClick={() => { setEditingId(q.id); setShowAddForm(false) }}>
                    ✏️ 編輯
                  </button>
                  <button className="btn btn-danger btn-sm" onClick={() => handleDelete(q.id)}>
                    🗑 刪除
                  </button>
                </div>
              </div>
              <div className="question-text">{q.questionText}</div>
              <div className="options" style={{ pointerEvents: 'none' }}>
                {['A', 'B', 'C', 'D'].map(opt => (
                  <div key={opt} className={`option${q.correctAnswer === opt ? ' correct' : ''}`}>
                    <span className="option-label"
                      style={q.correctAnswer === opt ? { background: 'var(--success)', color: 'white' } : {}}>
                      {opt}
                    </span>
                    <span>{q[`option${opt}`]}</span>
                    {q.correctAnswer === opt && (
                      <span style={{ marginLeft: 'auto', fontSize: '.75rem', color: 'var(--success)', fontWeight: 700 }}>
                        ✓ 正確答案
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )
        })
      )}

      {exam.questions.length > 0 && !showAddForm && !editingId && (
        <div style={{ textAlign: 'center', marginTop: '1rem', paddingBottom: '2rem' }}>
          <button className="btn btn-teacher" onClick={() => setShowAddForm(true)}>
            ＋ 繼續新增題目
          </button>
        </div>
      )}
    </>
  )
}
