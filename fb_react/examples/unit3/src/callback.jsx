import { useState } from 'react';

// memo：若 props 沒變，不重新渲染子元件
// const Button = memo(({ onClick, label }) => {
//   console.log(`渲染按鈕：${label}`);
//   return <button onClick={onClick}>{label}</button>;
// });
const Button = ({ onClick, label }) => {
  console.log(`渲染按鈕：${label}`);
  return <button onClick={onClick}>{label}</button>;
};

function CallbackDemo() {
  const [count, setCount] = useState(0);
  const [text, setText] = useState("");

  // ❌ 沒有 useCallback：每次 Parent 重渲染，handleClick 都是新函式
  // 導致 Button 也重渲染（即使 memo 也沒用）
   const handleClick = () => setCount(c => c + 1);

  // ✅ 有 useCallback：函式引用穩定，Button 不會無謂重渲染
//   const handleClick = useCallback(() => {
//     setCount(c => c + 1);
//   }, []); // 空依賴陣列 → 函式永遠不重建
  
  
  return (
    <div>
      <p>計數：{count}</p>
      <input value={text} onChange={e => setText(e.target.value)} />
      <Button onClick={handleClick} label="增加" />
    </div>
  );
}

export default CallbackDemo;