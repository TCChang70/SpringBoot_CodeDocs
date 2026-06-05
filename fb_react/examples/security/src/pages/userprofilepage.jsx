import { useAuth } from '../auth/authcontext';

function UserProfilePage() {
  const { user } = useAuth();
  return <h1>用戶資料 — {user?.username}（USER 或 ADMIN 可見）</h1>;
}
export default UserProfilePage;