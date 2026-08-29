import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { getStudents, getStudentClasses } from '../../api/examApi'

export default function StudentListPage() {
  const { auth } = useAuth()
  const [students, setStudents] = useState([])
  const [classes, setClasses] = useState([])
  const [filter, setFilter] = useState('')     // '' = 全部
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  // Load class list once
  useEffect(() => {
    getStudentClasses(auth.token)
      .then(setClasses)
      .catch(() => {})
  }, [auth.token])

  // Reload students when filter changes
  useEffect(() => {
    setLoading(true)
    getStudents(auth.token, filter || undefined)
      .then(setStudents)
      .catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }, [auth.token, filter])

  // Group students by className for the card view
  const grouped = students.reduce((acc, s) => {
    const key = s.className || '（未設定班級）'
    if (!acc[key]) acc[key] = []
    acc[key].push(s)
    return acc
  }, {})

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">學生名冊</h1>
          <p className="text-muted text-sm">共 {students.length} 位學生</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {/* Class filter */}
      <div style={{ display: 'flex', gap: '.75rem', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
        <span className="text-sm" style={{ fontWeight: 500, color: 'var(--gray-700)' }}>篩選班級：</span>
        <button
          className={`btn btn-sm ${filter === '' ? 'btn-teacher' : 'btn-ghost'}`}
          onClick={() => setFilter('')}
        >
          全部（{students.length}）
        </button>
        {classes.map(cls => {
          const count = students.filter(s => s.className === cls).length
          return (
            <button
              key={cls}
              className={`btn btn-sm ${filter === cls ? 'btn-teacher' : 'btn-ghost'}`}
              onClick={() => setFilter(cls)}
            >
              {cls}（{count}）
            </button>
          )
        })}
      </div>

      {loading ? (
        <div className="loading">⏳ 載入中...</div>
      ) : students.length === 0 ? (
        <div className="empty">
          <div className="empty-icon">👥</div>
          <p>尚無學生資料</p>
        </div>
      ) : filter ? (
        /* Flat table when filtering by one class */
        <div className="card">
          <h3 className="card-title" style={{ color: 'var(--teacher)', marginBottom: '1rem' }}>
            📚 {filter}
          </h3>
          <StudentTable students={students} />
        </div>
      ) : (
        /* Grouped cards when showing all */
        Object.entries(grouped).map(([cls, members]) => (
          <div key={cls} className="card" style={{ marginBottom: '1rem', borderLeft: '4px solid var(--teacher)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h3 style={{ fontWeight: 700, color: 'var(--teacher)' }}>📚 {cls}</h3>
              <span
                style={{ background: '#f3e8ff', color: '#7c3aed', padding: '.25rem .75rem', borderRadius: 999, fontSize: '.875rem', fontWeight: 600 }}
              >
                {members.length} 人
              </span>
            </div>
            <StudentTable students={members} />
          </div>
        ))
      )}
    </>
  )
}

function StudentTable({ students }) {
  return (
    <div className="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>顯示姓名</th>
            <th>帳號</th>
            <th>班級</th>
          </tr>
        </thead>
        <tbody>
          {students.map((s, idx) => (
            <tr key={s.id}>
              <td className="text-muted text-sm">{idx + 1}</td>
              <td style={{ fontWeight: 500 }}>{s.displayName}</td>
              <td className="text-muted">{s.username}</td>
              <td>
                {s.className
                  ? <span style={{ background: '#dbeafe', color: '#1e40af', padding: '.15rem .55rem', borderRadius: 999, fontSize: '.8rem', fontWeight: 500 }}>{s.className}</span>
                  : <span className="text-muted text-sm">—</span>
                }
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
