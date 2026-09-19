import { Link } from 'react-router-dom'
import { useAuth } from '../auth'

export default function Home() {
  const { user, isAdmin } = useAuth()

  const items = [{ to: '/orders', label: '點餐作業', desc: '桌位開單、點餐、結帳收款', icon: '🧾' }]
  if (isAdmin) {
    items.push(
      { to: '/employees', label: '員工管理', desc: '新增帳號、啟用停用', icon: '👥' },
      { to: '/tables', label: '桌位管理', desc: '新增桌位、變更狀態', icon: '🪑' },
      { to: '/menu', label: '菜單管理', desc: '菜色上架、價格調整', icon: '🍽️' },
      { to: '/closing', label: '每日結帳', desc: '結帳日報與對帳', icon: '📊' },
      { to: '/reports', label: '交易報表', desc: '交易明細與營收彙總', icon: '📈' },
    )
  }

  return (
    <div>
      <h2>主選單</h2>
      <p className="muted">
        {user?.name}，您好。請選擇要執行的功能。
      </p>
      <div className="cards">
        {items.map((it) => (
          <Link key={it.to} to={it.to} className="card link-card">
            <div className="card-icon">{it.icon}</div>
            <div className="card-title">{it.label}</div>
            <div className="muted">{it.desc}</div>
          </Link>
        ))}
      </div>
    </div>
  )
}