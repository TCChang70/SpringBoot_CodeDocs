// 使用 useFetch — 元件變得非常簡潔！
import useFetch from './useFetch';

function UserList() {
  const { data: users, loading, error } = useFetch(
    'https://jsonplaceholder.typicode.com/users'
  );

  if (loading) return <p>載入中...</p>;
  if (error) return <p>錯誤：{error}</p>;

  return (
    <ul>
      {users.map(user => <li key={user.id}>{user.name}</li>)}
    </ul>
  );
}

export default UserList;