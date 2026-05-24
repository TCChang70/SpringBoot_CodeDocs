import { useState, useEffect } from 'react';

function UserSearch() {
  const [users, setUsers] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('https://jsonplaceholder.typicode.com/users')
      .then(res => res.json())
      .then(data => {
        setUsers(data);
        setLoading(false);
      });
  }, []);

  // 根據 searchTerm 過濾使用者（不需要再呼叫 API）
  const filteredUsers = users.filter(user =>
    user.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div>
      <input
        type="text"
        placeholder="搜尋使用者..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
      />
      {loading ? (
        <p>載入中...</p>
      ) : (
        <ul>
          {filteredUsers.map(user => (
            <li key={user.id}>
              <strong>{user.name}</strong> — {user.email}
            </li>
          ))}
          {filteredUsers.length === 0 && <p>找不到符合的使用者</p>}
        </ul>
      )}
    </div>
  );
}

export default UserSearch;