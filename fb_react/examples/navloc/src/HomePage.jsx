import { useNavigate } from 'react-router-dom';

function HomePage() {
  const navigate = useNavigate();

  return (
    <div className="home-wrapper">
      <h1>歡迎回來！</h1>
      <p>您已成功登入。</p>
      <button className="login-btn" onClick={() => navigate('/login')}>
        登出
      </button>
    </div>
  );
}

export default HomePage;
