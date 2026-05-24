import { useMemo } from 'react';

export function ExpensiveList({ items, filter }) {
  // ❌ 沒有 useMemo：每次渲染都重新過濾（若 items 很大會很慢）
  // const filteredItems = items.filter(item => item.includes(filter));

  // ✅ 有 useMemo：只有 items 或 filter 改變時才重新計算
  const filteredItems = useMemo(() => {
    console.log('重新計算過濾結果...');
    return items.filter(item => item.toLowerCase().includes(filter.toLowerCase()));
  }, [items, filter]); // 依賴陣列

  return (
    <ul>
      {filteredItems.map((item, i) => <li key={i}>{item}</li>)}
    </ul>
  );
}

