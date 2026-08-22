//import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import 'bootstrap/dist/css/bootstrap.min.css' // Bootstrap 樣式
import 'bootstrap/dist/js/bootstrap.bundle.min.js' // Bootstrap 互動元件（navbar 展開、下拉選單等）
import './index.css'
//import App from './App.jsx'
import { BrowserRouter } from 'react-router-dom'
import AppRouter from './AppRouter.jsx'
createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <AppRouter />
  </BrowserRouter>
)
