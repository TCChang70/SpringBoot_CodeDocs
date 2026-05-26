import { useNavigate } from 'react-router-dom';

export default function Overview() {
  const navigate = useNavigate();

  return (
    <div>     
          <h1>OverView Page</h1>
          <p>這是使用 React Router 建立的左側面板。</p>
          <button            
            onClick={() => navigate('/products')}>
            瀏覽商品
          </button>
    </div>      
  );
}
