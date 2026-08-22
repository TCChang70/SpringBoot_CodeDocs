export default function About() {
  return (
    <div className="container">
      {/* card：將內容包在卡片中，置中顯示 */}
      <div className="card shadow-sm">
        <div className="card-body">
          <h1 className="card-title">關於我們</h1>
          <p className="card-text">
            這是 About 頁面，用來示範 React Router 的路由切換。
          </p>
        </div>
      </div>
    </div>
  );
}
