import './App.css'
import ProductCard, { CartIcon, CartSidebar } from './ProductCard'

const PRODUCTS = [
  { id: 1, name: '機械鍵盤', price: 1299 },
  { id: 2, name: '無線滑鼠', price: 599 },
  { id: 3, name: '27吋螢幕', price: 8990 },
  { id: 4, name: 'USB Hub', price: 450 },
]

function App() {
  return (
    <>
      <header className="app-header">
        <h1>🛍️ 線上商城</h1>
        <CartIcon />
      </header>

      <main className="product-grid">
        {PRODUCTS.map(product => (
          <ProductCard key={product.id} product={product} />
        ))}
      </main>

      <CartSidebar />
    </>
  )
}

export default App
