import { useEffect, useState } from 'react'

function Timer() {
  const [seconds, setSeconds] = useState(0)

  useEffect(() => {
    const id = setInterval(() => {
      setSeconds(s => s + 1)
    }, 1000)
    // Cleanup：元件卸載時清除計時器，避免記憶體洩漏
    return () => clearInterval(id)
  }, [])

  return <p>⏱️ 計時器已運行 <strong>{seconds}</strong> 秒</p>
}

function WindowSize() {
  const [size, setSize] = useState({ w: window.innerWidth, h: window.innerHeight })

  useEffect(() => {
    const onResize = () => setSize({ w: window.innerWidth, h: window.innerHeight })
    window.addEventListener('resize', onResize)
    // Cleanup：移除事件監聽器
    return () => window.removeEventListener('resize', onResize)
  }, [])

  return (
    <p>🖥️ 視窗大小：<strong>{size.w} × {size.h}</strong>（拖曳視窗改變大小試試）</p>
  )
}

export default function DemoUseEffectCleanup() {
  const [show, setShow] = useState(true)

  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 08 — useEffect Cleanup（清除副作用）</h2>

      <div className="card mb-3" style={{ maxWidth: 480 }}>
        <div className="card-body">
          {show ? <Timer /> : <p className="text-muted">計時器已卸載（不再跳動）</p>}
          {show && <WindowSize />}
          <button
            className="btn btn-warning btn-sm mt-2"
            onClick={() => setShow(v => !v)}
          >
            {show ? '卸載元件（觀察 cleanup）' : '重新掛載元件'}
          </button>
        </div>
      </div>

      <div className="log-box">
        <div className="log-info">// useEffect 回傳的函式就是 cleanup，元件「卸載」時執行</div>
        <div className="log-err">// ❌ 沒有 cleanup → setInterval 仍持續執行、resize 監聽器殘留（記憶體洩漏）</div>
        <div className="log-ok">// ✅ 有 cleanup → clearInterval / removeEventListener，完全停止、乾淨卸載</div>
      </div>
    </div>
  )
}
