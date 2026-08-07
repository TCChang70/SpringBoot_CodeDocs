import { useState } from 'react'
import Navbar from './components/Navbar'
import Login from './components/Login'
import Products from './components/Products'
import Cart from './components/Cart'
import Orders from './components/Orders'

// App 是根元件，負責管理全域共享狀態 (Global State)
function App() {
  const [currentPage, setCurrentPage] = useState('login')
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [username, setUsername] = useState('')
  const [cart, setCart] = useState([])

  function handleLoginSuccess(user) {
    sessionStorage.setItem('username', user)
    setIsLoggedIn(true)
    setUsername(user)
  }

  function addToCart(product, quantity) {
    setCart(prev => [...prev, { ...product, quantity: Number(quantity) }])
    alert(`已將 ${product.title} 加入購物車`)
  }

  function removeFromCart(index) {
    setCart(prev => prev.filter((_, i) => i !== index))
  }

  function clearCart() {
    setCart([])
  }

  return (
    <>
      <Navbar
        currentPage={currentPage}
        setCurrentPage={setCurrentPage}
        isLoggedIn={isLoggedIn}
        username={username}
      />
      <div className="container mt-4">
        {currentPage === 'login' && (
          <Login onLoginSuccess={handleLoginSuccess} />
        )}
        {currentPage === 'products' && (
          <Products addToCart={addToCart} />
        )}
        {currentPage === 'cart' && (
          <Cart
            cart={cart}
            removeFromCart={removeFromCart}
            clearCart={clearCart}
            isLoggedIn={isLoggedIn}
            username={username}
          />
        )}
        {currentPage === 'orders' && (
          <Orders isLoggedIn={isLoggedIn} username={username} />
        )}
      </div>
    </>
  )
}

export default App
