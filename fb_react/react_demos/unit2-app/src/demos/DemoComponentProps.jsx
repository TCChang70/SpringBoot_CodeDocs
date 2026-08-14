import PropTypes from 'prop-types'

function ProductCard({ name, price, inStock }) {
  return (
    <div className="card" style={{ width: '16rem' }}>
      <div className="card-body">
        <h5 className="card-title">{name}</h5>
        <p className="card-text">價格：${price}</p>
        {inStock ? (
          <span className="badge text-bg-success">有庫存</span>
        ) : (
          <span className="badge text-bg-danger">缺貨中</span>
        )}
      </div>
    </div>
  )
}

// Props 型別驗證：型別錯誤或缺少必填時，開發模式下 console 會跳出警告
ProductCard.propTypes = {
  name: PropTypes.string.isRequired,
  price: PropTypes.number.isRequired,
  inStock: PropTypes.bool,
}

// 未傳 inStock 時，預設值為 true
ProductCard.defaultProps = {
  inStock: true,
}

// Children Props：Card 用 children 包裹任意內容
function Card({ title, children }) {
  return (
    <div className="card" style={{ maxWidth: 480 }}>
      <div className="card-header"><strong>{title}</strong></div>
      <div className="card-body">{children}</div>
    </div>
  )
}

export default function DemoComponentProps() {
  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 03 — 元件、Props、Children、PropTypes</h2>

      <h5 className="mt-4">1. Props 傳遞與接收（單向流動，子元件唯讀）</h5>
      <div className="d-flex gap-3 flex-wrap">
        <ProductCard name="iPhone 16" price={999} inStock />
        <ProductCard name="AirPods Pro" price={249} inStock={false} />
        {/* 沒傳 inStock → 套用 defaultProps：true */}
        <ProductCard name="MacBook Air" price={1299} />
      </div>

      <h5 className="mt-4">2. Children Props（把內容放在開合標籤之間）</h5>
      <Card title="使用者資訊">
        <p className="mb-1">姓名：Alice</p>
        <p className="mb-2">Email：alice@example.com</p>
        <button className="btn btn-outline-primary btn-sm">編輯</button>
      </Card>

      <div className="log-box mt-4">
        <div className="log-info">// Props 是「單向流動」：父元件傳入，子元件只能讀、不能改</div>
        <div className="log-ok">// 第三張卡片未傳 inStock → 透過 defaultProps 顯示「有庫存」</div>
        <div className="log-info">// 試著把 price 改成字串「&quot;999&quot;」，Console 會出現 propTypes 警告</div>
      </div>
    </div>
  )
}
