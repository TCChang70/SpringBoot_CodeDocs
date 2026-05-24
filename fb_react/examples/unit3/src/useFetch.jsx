// useFetch.js
import { useReducer, useEffect } from 'react';

// 將三個相關狀態合併，單一 dispatch 避免 cascading render
const initialState = { data: null, loading: true, error: null };

function reducer(state, action) {
  switch (action.type) {
    case 'FETCH_START':   return { data: null,         loading: true,  error: null          };
    case 'FETCH_SUCCESS': return { data: action.data,  loading: false, error: null          };
    case 'FETCH_ERROR':   return { data: null,         loading: false, error: action.error  };
    default: return state;
  }
}

function useFetch(url) {
  const [state, dispatch] = useReducer(reducer, initialState);

  useEffect(() => {
    // AbortController：元件卸載時取消請求，避免 memory leak
    const controller = new AbortController();

    dispatch({ type: 'FETCH_START' }); // 單一 dispatch，不會 cascading

    fetch(url, { signal: controller.signal })
      .then(res => {
        if (!res.ok) throw new Error(`HTTP 錯誤：${res.status}`);
        return res.json();
      })
      .then(data => {
        dispatch({ type: 'FETCH_SUCCESS', data });
      })
      .catch(err => {
        // AbortError 是主動取消，不視為錯誤
        if (err.name !== 'AbortError') {
          dispatch({ type: 'FETCH_ERROR', error: err.message });
        }
      });

    return () => controller.abort(); // 清除函式：取消未完成的請求
  }, [url]); // url 改變時重新 fetch

  return state; // 回傳 { data, loading, error }
}

export default useFetch;
