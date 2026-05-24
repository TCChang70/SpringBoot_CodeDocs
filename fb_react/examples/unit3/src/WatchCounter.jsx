import { useState, useRef } from 'react';

export function WatchCounter() {
  const [time, setTime] = useState(0);
  const [running, setRunning] = useState(false);
  // 用 useRef 儲存 interval ID，避免因重渲染遺失
  const intervalRef = useRef(null);

  const start = () => {
    if (running) return;
    setRunning(true);
    intervalRef.current = setInterval(() => {
      setTime(prev => prev + 1);
    }, 1000);
  };

  const stop = () => {
    clearInterval(intervalRef.current);
    setRunning(false);
  };

  const reset = () => {
    stop();
    setTime(0);
  };

  return (
    <div>
      <p>{time} 秒</p>
      <button onClick={start} disabled={running}>開始</button>
      <button onClick={stop} disabled={!running}>暫停</button>
      <button onClick={reset}>重置</button>
    </div>
  );
}