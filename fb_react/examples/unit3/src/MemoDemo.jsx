// MemoDemo.jsx — ExpensiveList 的使用範例
import { useState } from 'react';
import { ExpensiveList } from './memo';

// 模擬一份大型城市清單
const CITIES = [
  'Tokyo', 'Taipei', 'Taichung', 'Tainan', 'Taitung',
  'New York', 'New Delhi', 'New Orleans', 'Newcastle',
  'Beijing', 'Berlin', 'Bangkok', 'Barcelona', 'Brisbane',
  'Seoul', 'Sydney', 'Singapore', 'Stockholm', 'Santiago',
];

export default function MemoDemo() {
  const [filter, setFilter] = useState('');
  const [count, setCount]   = useState(0);   // ← 刻意加一個「無關的狀態」

  return (
    <div style={{ maxWidth: 480, margin: '40px auto', fontFamily: 'sans-serif' }}>
      <h2>useMemo 示範</h2>

      <p style={{ color: '#666', fontSize: 14 }}>
        點擊「+1」改變 <code>count</code>，觀察 console：
        因為 <code>items</code> / <code>filter</code> 沒變，
        <strong> useMemo 不會重新計算</strong>。
      </p>

      {/* 無關的狀態，觸發父元件重新渲染 */}
      <div style={{ marginBottom: 16 }}>
        <button
          onClick={() => setCount(c => c + 1)}
          style={{ padding: '6px 16px', marginRight: 8 }}
        >
          +1
        </button>
        <span>count = {count}（改變此值不會重算過濾）</span>
      </div>

      {/* 過濾輸入框 */}
      <input
        type="text"
        value={filter}
        onChange={e => setFilter(e.target.value)}
        placeholder="輸入關鍵字過濾城市..."
        style={{ width: '100%', padding: '8px 12px', fontSize: 16, boxSizing: 'border-box', marginBottom: 12 }}
      />

      <p style={{ fontSize: 13, color: '#888' }}>
        ↳ 只有輸入框改變時，才會看到 console 印出「重新計算過濾結果...」
      </p>

      {/* 將 CITIES 與 filter 傳入 ExpensiveList */}
      <ExpensiveList items={CITIES} filter={filter} />
    </div>
  );
}
