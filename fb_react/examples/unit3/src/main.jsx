import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
//import AppContext from './AppContext.jsx';
//import ShoppingCart from './ShopReducer.jsx';
//import MemoDemo from './MemoDemo.jsx';
//import CallbackDemo from './callback.jsx';
//import UserList from './fetchList.jsx';
//import PostList from './fetchPostList'
import MySettings from './mystorage'
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <MySettings />
  </StrictMode>
)
