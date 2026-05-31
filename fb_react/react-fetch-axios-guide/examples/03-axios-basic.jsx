// 範例 03：Axios 基礎 — 最小可執行範例
// 需先安裝：npm install axios

import { useEffect } from 'react';
import axios from 'axios';

function AxiosBasic() {
  useEffect(() => {
    // Axios：更簡潔！直接取得 data，不需要手動解析 JSON
    axios.get('https://jsonplaceholder.typicode.com/posts/1')
      .then(response => {
        // response.data 就是你的 JSON 資料（Axios 自動解析）
        const post = response.data;
        console.log('文章 ID：', post.id);
        console.log('標題：', post.title);
      })
      .catch(error => {
        // Axios 的錯誤物件更豐富
        if (error.response) {
          // 伺服器有回應，但是錯誤狀態碼（4xx, 5xx）
          console.error('伺服器回應錯誤：', error.response.status);
        } else if (error.request) {
          // 請求有送出，但沒有收到回應
          console.error('網路無回應');
        } else {
          // 請求設定發生錯誤
          console.error('請求錯誤：', error.message);
        }
      });
  }, []);

  return <div>打開 DevTools Console 查看輸出 👆</div>;
}

export default AxiosBasic;

/*
Axios vs Fetch 的關鍵差異：

Fetch:
  const response = await fetch(url);   ← 取得 Response 物件
  const data = await response.json();  ← 必須再次解析

Axios:
  const response = await axios.get(url); ← response.data 直接就是資料
  const data = response.data;            ← 省掉 .json() 這步

錯誤差異：
  Fetch: 404 不會自動拋出例外，需手動 if (!response.ok)
  Axios: 404 自動拋出例外，直接進 catch
*/
