// components/UserDashboard.jsx
import { useState, useMemo, useCallback } from 'react';
import { useApp } from './userapp';
import useFetch from './useFetch';

export default function UserDashboard() {
  const { state, dispatch } = useApp();
  const { data: users, loading, error } = useFetch(
    'https://jsonplaceholder.typicode.com/users'
  );
  const [search, setSearch] = useState('');

  const filteredUsers = useMemo(() =>
    (users ?? []).filter(u =>
      u.name.toLowerCase().includes(search.toLowerCase())
    ),
    [users, search]
  );

  const handleSelect = useCallback((user) => {
    dispatch({ type: 'SELECT_USER', payload: user });
  }, [dispatch]);

  return (
    <div style={{ background: state.theme === 'dark' ? '#1a1a1a' : '#fff', color: state.theme === 'dark' ? '#fff' : '#000', minHeight: '100vh', padding: '20px' }}>
      <button onClick={() => dispatch({ type: 'TOGGLE_THEME' })}>
        切換 {state.theme === 'light' ? '深色' : '淺色'} 主題
      </button>

      <input
        value={search}
        onChange={e => setSearch(e.target.value)}
        placeholder="搜尋使用者..."
      />

      {loading && <p>載入中...</p>}
      {error && <p>錯誤：{error}</p>}

      <ul>
        {filteredUsers.map(user => (
          <li key={user.id} onClick={() => handleSelect(user)} style={{ cursor: 'pointer' }}>
            {user.name}
          </li>
        ))}
      </ul>

      {state.selectedUser && (
        <div>
          <h3>選取的使用者</h3>
          <p>名稱：{state.selectedUser.name}</p>
          <p>Email：{state.selectedUser.email}</p>
        </div>
      )}
    </div>
  );
}