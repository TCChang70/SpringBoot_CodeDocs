import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
//import App from './App.jsx'
import UserManager from './components/UserManager.jsx';
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <UserManager />
  </StrictMode>
)
