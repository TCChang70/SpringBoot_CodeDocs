import { useNavigate } from 'react-router-dom';

export default function Home() {
  const navigate = useNavigate();

  return (
    <div>     
          <h1>歡迎來到首頁</h1>
          <p>這是使用 React Router 建立的 SPA 示範專案。</p>
          <button            
            onClick={() => navigate('/products')}>
            瀏覽商品
          </button>
    </div>      
  );
}
