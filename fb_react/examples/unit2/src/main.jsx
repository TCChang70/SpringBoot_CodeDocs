import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
//import App from './App.jsx'
//import Exp from './Exp.jsx'
//import ShowGoods from './goodslist.jsx'
//import AppProperty from './property.jsx'
//import AppChild from './child_property'
//import { Counter , ProfileForm } from './Counter'
//import TodoList from './todos'
//import LoginForm from './FormState'
//import UserList from './effect'
import UserSearch from './searchapp'
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <UserSearch/>
  </StrictMode>
)
