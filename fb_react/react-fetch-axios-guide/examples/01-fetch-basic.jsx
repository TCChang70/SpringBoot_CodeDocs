// 範例 01：Fetch 基礎 — 最小可執行範例
// 貼入 App.jsx 並執行，觀察 Console 輸出

import { useEffect } from 'react';

function App() {
  useEffect(() => {
    // ① fetch() 回傳 Promise<Response>
    fetch('https://jsonplaceholder.typicode.com/posts/1')
      // ② 解析 Response 為 JSON（也是非同步的）
      .then(response => response.json())
      // ③ 使用資料
      .then(post => {
        console.log('文章 ID：', post.id);
        console.log('標題：', post.title);
        console.log('內容：', post.body);
      })
      // ④ 捕捉網路錯誤（注意：HTTP 錯誤不會在這裡被捕捉）
      .catch(error => {
        console.error('取得資料失敗：', error);
      });
  }, []);

  return <div>打開 DevTools Console 查看輸出 👆</div>;
}

export default App;

/*
預期輸出：
文章 ID：1
標題：sunt aut facere repellat provident occaecati excepturi optio reprehenderit
內容：quia et suscipit...

學習重點：
- fetch() 是非同步的，使用 .then() 鏈接後續操作
- 需要呼叫 .json() 才能取得 JavaScript 物件
- .catch() 只捕捉網路層級的錯誤，不包含 HTTP 4xx/5xx
*/
