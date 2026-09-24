// 路由表：前台公開 + 作者工作區 + admin/editor 管理台
import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';

import Home from './pages/Home.jsx';
import ArticleDetail from './pages/ArticleDetail.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Profile from './pages/Profile.jsx';
import MyArticles from './pages/MyArticles.jsx';
import ArticleEdit from './pages/ArticleEdit.jsx';
import AdminArticles from './pages/AdminArticles.jsx';
import Moderation from './pages/Moderation.jsx';
import CommentsAdmin from './pages/CommentsAdmin.jsx';
import CategoriesAdmin from './pages/CategoriesAdmin.jsx';
import TagsAdmin from './pages/TagsAdmin.jsx';
import MediaLibrary from './pages/MediaLibrary.jsx';
import UsersAdmin from './pages/UsersAdmin.jsx';

const authorOrAbove = ['author', 'editor', 'admin'];
const editorOrAbove = ['editor', 'admin'];

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        {/* 公開 */}
        <Route path="/" element={<Home />} />
        <Route path="/articles/:id" element={<ArticleDetail />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* 需登入（author+） */}
        <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
        <Route path="/my" element={<ProtectedRoute roles={authorOrAbove}><MyArticles /></ProtectedRoute>} />
        <Route path="/media" element={<ProtectedRoute roles={authorOrAbove}><MediaLibrary /></ProtectedRoute>} />
        <Route path="/editor/new" element={<ProtectedRoute roles={authorOrAbove}><ArticleEdit /></ProtectedRoute>} />
        <Route path="/editor/:id" element={<ProtectedRoute roles={authorOrAbove}><ArticleEdit /></ProtectedRoute>} />

        {/* admin / editor */}
        <Route path="/admin/articles" element={<ProtectedRoute roles={editorOrAbove}><AdminArticles /></ProtectedRoute>} />
        <Route path="/moderation" element={<ProtectedRoute roles={editorOrAbove}><Moderation /></ProtectedRoute>} />
        <Route path="/admin/comments" element={<ProtectedRoute roles={editorOrAbove}><CommentsAdmin /></ProtectedRoute>} />
        <Route path="/admin/categories" element={<ProtectedRoute roles={editorOrAbove}><CategoriesAdmin /></ProtectedRoute>} />
        <Route path="/admin/tags" element={<ProtectedRoute roles={editorOrAbove}><TagsAdmin /></ProtectedRoute>} />

        {/* 僅 admin */}
        <Route path="/admin/users" element={<ProtectedRoute roles={['admin']}><UsersAdmin /></ProtectedRoute>} />
      </Route>
    </Routes>
  );
}