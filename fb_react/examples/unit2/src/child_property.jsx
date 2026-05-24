// 使用 children 讓元件包裹任意內容
function Card({ title, children }) {
  return (
    <div className="card">
      <h2>{title}</h2>
      <div className="card-body">
        {children}  {/* 渲染被包裹的內容 */}
      </div>
    </div>
  );
}

// 使用：把內容放在開合標籤之間
function AppChild() {
  return (
    <Card title="使用者資訊">
      <p>姓名：Alice</p>
      <p>Email：alice@example.com</p>
      <button>編輯</button>
    </Card>
  );
}

export default AppChild;