// 在 React 元件中使用
import { useState, useEffect } from 'react';
import { getUsers } from './api/userapp';

function UserList() {
  const [users, setUsers] = useState([]);

  useEffect(() => {
    getUsers(5).then(({ data }) => setUsers(data));
  }, []);

  return <ul>{users.map(u => <li key={u.id}>{u.username}</li>)}</ul>;
}

export default UserList;