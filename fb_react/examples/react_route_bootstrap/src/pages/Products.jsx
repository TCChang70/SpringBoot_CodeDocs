import { Link } from 'react-router-dom';

function Products() {
  const products = [
    { id: 1, name: 'Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops' },
    { id: 2, name: 'Mens Casual Premium Slim Fit T-Shirts ' },
    { id: 3, name: 'Mens Cotton Jacket' },
  ];

  return (
    <div className="container">
      <h2 className="mb-4">商品列表</h2>
      {/* row + col：Bootstrap 格線系統，每張卡片佔 12/12、6/6、4 欄（依螢幕寬度） */}
      <div className="row g-4">
        {products.map(p => (
          <div className="col-12 col-md-6 col-lg-4" key={p.id}>
            {/* card：卡片元件 */}
            <div className="card h-100 shadow-sm">
              <div className="card-body d-flex flex-column">
                <h5 className="card-title">{p.name}</h5>
                <Link
                  to={`/fakeproductdetail/${p.id}`}
                  className="btn btn-outline-primary mt-auto">
                  查看詳情
                </Link>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Products;
