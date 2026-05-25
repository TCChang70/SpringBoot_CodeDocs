export default function About() {
  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-12 col-md-8">
          <h1 className="mb-4">關於我們</h1>
          <p className="lead">我們是一個專注於 React 學習的團隊，致力於提供高品質的前端教學資源。</p>
          <ul className="list-group list-group-flush mt-3">
            <li className="list-group-item">⚡ React Router 路由管理</li>
            <li className="list-group-item">🧩 元件化開發架構</li>
            <li className="list-group-item">🪝 Hooks 狀態管理</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
