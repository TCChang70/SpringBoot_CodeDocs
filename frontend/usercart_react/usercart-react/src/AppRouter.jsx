// AppRouter：React Router 版根元件（不修改原 App.jsx）
import { useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import NavbarRouter from './components/NavbarRouter'
import Login from './components/Login'
import Products from './components/Products'
import Cart from './components/Cart'
import Orders from './components/Orders'

function AppRouter() {
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
    <BrowserRouter>
      <NavbarRouter isLoggedIn={isLoggedIn} username={username} />
      <div className="container mt-4">
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route
            path="/login"
            element={<Login onLoginSuccess={handleLoginSuccess} />}
          />
          <Route path="/products" element={<Products addToCart={addToCart} />} />
          <Route
            path="/cart"
            element={
              <Cart
                cart={cart}
                removeFromCart={removeFromCart}
                clearCart={clearCart}
                isLoggedIn={isLoggedIn}
                username={username}
              />
            }
          />
          <Route
            path="/orders"
            element={<Orders isLoggedIn={isLoggedIn} username={username} />}
          />
        </Routes>
      </div>
    </BrowserRouter>
  )
}

export default AppRouter
