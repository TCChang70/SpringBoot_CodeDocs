// src/components/UserManager.jsx
import { useState, useEffect } from 'react';
import axios from 'axios';

const BASE_URL = 'https://fakestoreapi.com/users';

export default function UserManager() {
  const [users, setUsers]     = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState(null);
  const [newName, setNewName] = useState('');

  // ── GET：取得使用者列表（含 cleanup）──────────────
  useEffect(() => {
    const controller = new AbortController();

    const fetchUsers = async () => {
      try {
        setLoading(true);
        const { data } = await axios.get(BASE_URL, {
          params: { limit: 20 },
          signal: controller.signal,
        });
        setUsers(data);
      } catch (err) {
        if (!axios.isCancel(err)) setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
    return () => controller.abort(); // cleanup
  }, []);

  // ── POST：新增使用者 ────────────────────────────
  const handleAdd = async () => {
    if (!newName.trim()) return;
    try {
      const { data: newUser } = await axios.post(BASE_URL, {
        username: newName,
        email: `${newName}@demo.com`,
        password: 'demo1234'       
      });
      // fakestoreapi 回傳新物件（含 id），附加到列表最前方
      setUsers(prev => [{...newUser,username: newName,
        email: `${newName}@demo.com`,
        password: 'demo1234' }, ...prev]);
      setNewName('');
    } catch (err) {
      setError(`新增失敗：${err.message}`);
    }
  };

  if (loading) return <p>載入中...</p>;
  if (error)   return <p style={{ color: 'red' }}>錯誤：{error}</p>;

  return (
    <div>
      <h2>使用者列表</h2>

      {/* 新增表單 */}
      <div>
        <input
          value={newName}
          onChange={e => setNewName(e.target.value)}
          placeholder="輸入 username"
        />
        <button onClick={handleAdd}>新增</button>
      </div>

      {/* 使用者列表 */}
      <ul>
        {users.map(user => (
          <li key={user.id}>
            [{user.id}] <strong>{user.username}</strong> — {user.email}
          </li>
        ))}
      </ul>
    </div>
  );
}