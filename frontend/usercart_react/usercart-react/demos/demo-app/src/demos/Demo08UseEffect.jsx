import { useState, useEffect } from 'react'

async function fetchProducts() {
  const res = await fetch('https://dummyjson.com/products?limit=4&select=id,title,price,thumbnail')
  if (!res.ok) throw new Error('載入失敗')
  const data = await res.json()
  return data.products
}

function Products({ addToCart }) {
  const [products, setProducts] = useState([])
  const [quantities, setQuantities] = useState({})
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchProducts()
      .then(data => {
        setProducts(data)
        const initQty = {}
        data.forEach(p => { initQty[p.id] = 1 })
        setQuantities(initQty)
      })
      .catch(err => alert(err.message))
      .finally(() => setLoading(false))
  }, [])

  function handleQtyChange(productId, value) {
    setQuantities(prev => ({ ...prev, [productId]: Number(value) }))
  }

  if (loading) return <div className="spinner-border text-primary" role="status" />

  return (
    <div className="row">
      {products.map(product => (
        <div className="col-md-3" key={product.id}>
          <div className="card mb-3">
            <img src={product.thumbnail} className="card-img-top"
              style={{ height: '120px', objectFit: 'cover' }} alt={product.title} />
            <div className="card-body">
              <h6 className="card-title">{product.title}</h6>
              <p className="card-text text-muted">$ {product.price}</p>
              <div className="d-flex gap-2">
                <input type="number" min="1" max="10"
                  value={quantities[product.id] ?? 1}
                  onChange={e => handleQtyChange(product.id, e.target.value)}
                  style={{ width: '60px' }} className="form-control form-control-sm" />
                <button className="btn btn-success btn-sm"
                  onClick={() => addToCart(product, quantities[product.id] ?? 1)}>加入</button>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

export default function Demo08UseEffect() {
  const [cart, setCart] = useState([])
  function addToCart(product, qty) {
    setCart(prev => [...prev, { ...product, quantity: qty }])
    alert(`已將 ${product.title} × ${qty} 加入購物車`)
  }
  return (
    <div className="container mt-4">
      <h2 className="text-primary">Demo 08 — useEffect 與物件 state</h2>
      <Products addToCart={addToCart} />
      {cart.length > 0 && (
        <div className="alert alert-success mt-3">
          🛒 {cart.map(i => `${i.title} × ${i.quantity}`).join('、')}
        </div>
      )}
    </div>
  )
}
