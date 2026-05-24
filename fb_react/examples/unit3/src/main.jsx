import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
//import AppContext from './AppContext.jsx';
//import ShoppingCart from './ShopReducer.jsx';
//import MemoDemo from './MemoDemo.jsx';
//import CallbackDemo from './callback.jsx';
import UserList from './fetchList.jsx';
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <UserList />
  </StrictMode>
)
