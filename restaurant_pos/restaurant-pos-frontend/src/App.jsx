import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth'
import Layout from './components/Layout'
import Login from './pages/Login'
import Home from './pages/Home'
import OrderFlow from './pages/OrderFlow'
import Employees from './pages/Employees'
import Tables from './pages/Tables'
import MenuAdmin from './pages/MenuAdmin'
import Closing from './pages/Closing'
import Reports from './pages/Reports'

function RequireAuth({ children }) {
  const { user } = useAuth()
  return user ? children : <Navigate to="/login" replace />
}

function RequireAdmin({ children }) {
  const { isAdmin } = useAuth()
  return isAdmin ? children : <Navigate to="/" replace />
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            element={
              <RequireAuth>
                <Layout />
              </RequireAuth>
            }
          >
            <Route path="/" element={<Home />} />
            <Route path="/orders" element={<OrderFlow />} />
            <Route path="/orders/:id" element={<OrderFlow />} />
            <Route
              path="/employees"
              element={
                <RequireAdmin>
                  <Employees />
                </RequireAdmin>
              }
            />
            <Route
              path="/tables"
              element={
                <RequireAdmin>
                  <Tables />
                </RequireAdmin>
              }
            />
            <Route
              path="/menu"
              element={
                <RequireAdmin>
                  <MenuAdmin />
                </RequireAdmin>
              }
            />
            <Route
              path="/closing"
              element={
                <RequireAdmin>
                  <Closing />
                </RequireAdmin>
              }
            />
            <Route
              path="/reports"
              element={
                <RequireAdmin>
                  <Reports />
                </RequireAdmin>
              }
            />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}