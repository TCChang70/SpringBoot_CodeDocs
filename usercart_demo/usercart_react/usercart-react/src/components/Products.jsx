import { useState, useEffect } from 'react'
import { fetchProducts } from '../api/apiService'

function Products({ addToCart }) {
  const [products, setProducts] = useState([])
  // 每個產品各自獨立的數量 state，以 productId 為 key
  const [quantities, setQuantities] = useState({})
  // 分頁 state
  const [currentPage, setCurrentPage] = useState(1)
  const perPage = 4

  // useEffect 替代 jQuery 的 document.ready + loadProducts()
  useEffect(() => {
    fetchProducts().then(data => {
      setProducts(data)
      // 初始化每個產品數量為 1
      const initQty = {}
      data.forEach(p => { initQty[p.id] = 1 })
      setQuantities(initQty)
    })
  }, []) // 空陣列 = 只在元件掛載時執行一次

  function handleQtyChange(productId, value) {
    setQuantities(prev => ({ ...prev, [productId]: Number(value) }))
  }

  const totalPages = Math.ceil(products.length / perPage)
  const currentProducts = products.slice((currentPage - 1) * perPage, currentPage * perPage)
  const pageNumbers = Array.from({ length: totalPages }, (_, i) => i + 1)

  function handlePageChange(page) {
    setCurrentPage(page)
  }

  return (
    <div>
      <h3>產品列表</h3>
      <div className="row">
        {currentProducts.map(product => (
          <div className="col-md-3" key={product.id}>
            <div className="card mb-3">
              <div className="card-body">
                <h5 className="card-title">{product.title}</h5>
                <img
                  src={product.image}
                  className="card-img-top"
                  width="160"
                  height="200"
                  alt={product.title}
                />
                <p className="card-text">價格：{product.price} 元</p>
                <p className="card-text">
                  購買數量：
                  <input
                    type="number"
                    min="1"
                    value={quantities[product.id] ?? 1}
                    onChange={e => handleQtyChange(product.id, e.target.value)}
                    style={{ width: '60px' }}
                  />
                </p>
                <button
                  className="btn btn-success"
                  onClick={() => addToCart(product, quantities[product.id] ?? 1)}
                >
                  加入購物車
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
      {totalPages > 1 && (
        <nav>
          <ul className="pagination justify-content-center">
            <li className={`page-item ${currentPage === 1 ? 'disabled' : ''}`}>
              <button className="page-link" onClick={() => handlePageChange(currentPage - 1)}>
                上一頁
              </button>
            </li>
            {pageNumbers.map(page => (
              <li className={`page-item ${currentPage === page ? 'active' : ''}`} key={page}>
                <button className="page-link" onClick={() => handlePageChange(page)}>
                  {page}
                </button>
              </li>
            ))}
            <li className={`page-item ${currentPage === totalPages ? 'disabled' : ''}`}>
              <button className="page-link" onClick={() => handlePageChange(currentPage + 1)}>
                下一頁
              </button>
            </li>
          </ul>
        </nav>
      )}
    </div>
  )
}

export default Products
