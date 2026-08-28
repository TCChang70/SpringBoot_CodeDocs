import { Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import MenuManager from './pages/MenuManager'
import CreateOrder from './pages/CreateOrder'
import Orders from './pages/Orders'

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="menu" element={<MenuManager />} />
        <Route path="order" element={<CreateOrder />} />
        <Route path="orders" element={<Orders />} />
      </Route>
    </Routes>
  )
}

export default App