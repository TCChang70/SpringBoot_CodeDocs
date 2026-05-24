import { useRef } from 'react';

export function SearchBox() {
  // 建立 ref，初始值為 null
  const inputRef = useRef(null);

  const focusInput = () => {
    // 透過 .current 存取真實 DOM 元素
    inputRef.current.focus();
  };

  const clearInput = () => {
    inputRef.current.value = "";
    inputRef.current.focus();
  };

  return (
    <div>
      {/* 用 ref 屬性綁定 DOM 元素 */}
      <input ref={inputRef} type="text" placeholder="搜尋..." />
      <button onClick={focusInput}>聚焦</button>
      <button onClick={clearInput}>清除</button>
    </div>
  );
}