# React Context 教學文件

## 目錄
- [React Context 教學文件](#react-context-教學文件)
  - [目錄](#目錄)
  - [1. 什麼是 React Context？](#1-什麼是-react-context)
  - [2. 問題：Prop Drilling（屬性探鑽）](#2-問題prop-drilling屬性探鑽)
    - [示意圖](#示意圖)
    - [問題程式碼](#問題程式碼)
  - [3. 解決方法：建立 Context](#3-解決方法建立-context)
    - [步驟 1：建立 Context 檔案](#步驟-1建立-context-檔案)
    - [步驟 2：使用 Provider 提供資料](#步驟-2使用-provider-提供資料)
  - [4. Context 的三個核心概念](#4-context-的三個核心概念)
  - [5. 範例一：使用 Consumer 讀取 Context](#5-範例一使用-consumer-讀取-context)
    - [說明](#說明)
    - [資料流示意](#資料流示意)
  - [6. 範例二：使用 useContext Hook](#6-範例二使用-usecontext-hook)
    - [改寫前（Consumer 寫法）](#改寫前consumer-寫法)
    - [改寫後（useContext 寫法）](#改寫後usecontext-寫法)
    - [完整的多層元件範例（使用 useContext）](#完整的多層元件範例使用-usecontext)
  - [7. 範例三：搭配 useState 更新 Context 值（本地狀態）](#7-範例三搭配-usestate-更新-context-值本地狀態)
    - [狀態說明](#狀態說明)
  - [8. 進階技巧：透過 Context 共享更新函式](#8-進階技巧透過-context-共享更新函式)
    - [8.1 正確模式：將 setter 傳入 Context](#81-正確模式將-setter-傳入-context)
    - [8.2 最佳實踐：自定義 Hook 封裝](#82-最佳實踐自定義-hook-封裝)
  - [9. 完整綜合範例](#9-完整綜合範例)
    - [檔案結構](#檔案結構)
    - [MyUserContext.js](#myusercontextjs)
    - [UserApp.jsx](#userappjsx)
    - [UserProfile.jsx（Consumer 方式）](#userprofilejsxconsumer-方式)
    - [UpdateUsername.jsx（useContext 方式）](#updateusernamejsxusecontext-方式)
  - [10. 重點整理](#10-重點整理)
    - [React Context 使用步驟](#react-context-使用步驟)
    - [三種讀取方式比較](#三種讀取方式比較)
    - [常見錯誤](#常見錯誤)
    - [效能注意事項](#效能注意事項)
    - [何時使用 Context？](#何時使用-context)

---

## 1. 什麼是 React Context？

**React Context** 是一種在 React 應用中進行**全域狀態管理**的機制。

- 讓資料可以在元件樹中**任意層級**的元件之間共享，而不需要逐層透過 `props` 傳遞。
- 適合用來管理「跨多個層級都需要使用」的資料，例如：使用者資訊、語言設定、主題樣式等。
- 搭配 `useState` Hook，可以更輕鬆地在深度嵌套的元件之間共享與管理狀態。

---

## 2. 問題：Prop Drilling（屬性探鑽）

在沒有 Context 的情況下，若要讓深層的子元件取得資料，必須將 `props` 一層一層往下傳遞，即使中間的元件完全不需要用到該資料，這個問題稱為 **Prop Drilling（屬性探鑽）**。

### 示意圖

```
Component1 (擁有 user 狀態)
  └─ Component2 (傳遞 user props，自己不使用)
       └─ Component3 (傳遞 user props，自己不使用)
            └─ Component4 (傳遞 user props，自己不使用)
                 └─ Component5 (真正使用 user 的地方)
```

### 問題程式碼

```jsx
import { useState } from "react";

function Component1() {
  const [user, setUser] = useState("Jesse Hall");

  return (
    <>
      <h1>{`Hello ${user}!`}</h1>
      <Component2 user={user} />
    </>
  );
}

function Component2({ user }) {
  return (
    <>
      <h1>Component 2</h1>
      <Component3 user={user} />  {/* 只是傳遞，自己不使用 */}
    </>
  );
}

function Component3({ user }) {
  return (
    <>
      <h1>Component 3</h1>
      <Component4 user={user} />  {/* 只是傳遞，自己不使用 */}
    </>
  );
}

function Component4({ user }) {
  return (
    <>
      <h1>Component 4</h1>
      <Component5 user={user} />  {/* 只是傳遞，自己不使用 */}
    </>
  );
}

function Component5({ user }) {
  return (
    <>
      <h1>Component 5</h1>
      <h2>{`Hello ${user} again!`}</h2>  {/* 真正使用的地方 */}
    </>
  );
}

export default Component1;
```

> **問題點：** Component2、3、4 完全不需要 `user`，但仍必須接收並傳遞它，造成程式碼冗餘且難以維護。

---

## 3. 解決方法：建立 Context

使用 React Context 可以跳過中間層，讓需要資料的元件直接取用。

### 步驟 1：建立 Context 檔案

```js
// MyUserContext.js
import { createContext } from "react";

const UserContext = createContext();       // 無預設值
// const UserContext = createContext("Guest");  // 有預設值（可選）

export default UserContext;
```

`createContext(defaultValue?)` 接受一個**可選的預設值**參數，並回傳一個 **Context 物件**，其中包含兩個重要屬性：

| 屬性 | 說明 |
|------|------|
| `Provider` | 提供 Context 值的元件，包裹需要共享資料的子元件 |
| `Consumer` | 消費（讀取）Context 值的元件 |

> ⚠️ **預設值的作用：** `defaultValue` 只有在元件**沒有被任何 `Provider` 包裹**時才會生效。若 `Provider` 的 `value` 設為 `undefined`，子元件收到的也是 `undefined`，而非預設值。

### 步驟 2：使用 Provider 提供資料

```jsx
// UserApp.jsx
import React from "react";
import UserContext from "./MyUserContext";
import UserProfile from "./UserProfile";
import UpdateUsername from "./UpdateUsername";

function UserApp() {
  return (
    <UserContext.Provider value="John Doe">
      <UserProfile />
      <UpdateUsername />
    </UserContext.Provider>
  );
}

export default UserApp;
```

- `UserContext.Provider` 包裹所有需要使用該 Context 的子元件。
- `value` 屬性指定要共享的資料值。
- 當 `Provider` 的 `value` 改變時，所有使用該 Context 的子元件都會重新渲染。

---

## 4. Context 的三個核心概念

```
createContext()  →  建立 Context 物件
    │
    ├── Provider  →  提供資料（包裹子元件）
    │
    └── Consumer / useContext  →  讀取資料（在子元件中使用）
```

---

## 5. 範例一：使用 Consumer 讀取 Context

**`UserContext.Consumer`** 是一個 React 元件，接收一個函式作為子元件（Render Props 模式），函式的參數即為 Context 的值。

```jsx
// UserProfile.jsx
import React from "react";
import UserContext from "./MyUserContext";

function UserProfile() {
  return (
    <UserContext.Consumer>
      {(username) => (
        <div>
          <h1>User Profile</h1>
          <p>Username: {username}</p>
        </div>
      )}
    </UserContext.Consumer>
  );
}

export default UserProfile;
```

### 說明

1. `UserContext.Consumer` 訂閱了 `UserContext`。
2. 子函式 `(username) => (...)` 接收 `UserApp` 中 `Provider` 設定的 `value="John Doe"`。
3. 因此 `username` 的值為 `"John Doe"`，並渲染在畫面上。

### 資料流示意

```
UserApp
  └─ UserContext.Provider value="John Doe"
       └─ UserProfile
            └─ UserContext.Consumer
                 └─ (username) => <p>{username}</p>
                              ↑
                         "John Doe"
```

---

## 6. 範例二：使用 useContext Hook

**`useContext`** 是更簡潔的讀取 Context 的方式，推薦在函式元件中使用。

### 改寫前（Consumer 寫法）
```jsx
function UserProfile() {
  return (
    <UserContext.Consumer>
      {(username) => <p>Username: {username}</p>}
    </UserContext.Consumer>
  );
}
```

### 改寫後（useContext 寫法）
```jsx
import { useContext } from "react";
import UserContext from "./MyUserContext";

function UserProfile() {
  const username = useContext(UserContext);  // 直接取得值

  return <p>Username: {username}</p>;
}
```

### 完整的多層元件範例（使用 useContext）

```jsx
import { useState, createContext, useContext } from "react";

const UserContext = createContext();

function Component1() {
  const [user, setUser] = useState("Jesse Hall");

  return (
    <UserContext.Provider value={user}>
      <h1>{`Hello ${user}!`}</h1>
      <Component2 />  {/* 不再需要傳遞 props */}
    </UserContext.Provider>
  );
}

function Component2() {
  return (
    <>
      <h1>Component 2</h1>
      <Component3 />  {/* 不需要 user props */}
    </>
  );
}

function Component3() {
  return (
    <>
      <h1>Component 3</h1>
      <Component4 />  {/* 不需要 user props */}
    </>
  );
}

function Component4() {
  return (
    <>
      <h1>Component 4</h1>
      <Component5 />  {/* 不需要 user props */}
    </>
  );
}

function Component5() {
  const user = useContext(UserContext);  // 直接從 Context 取得值！

  return (
    <>
      <h1>Component 5</h1>
      <h2>{`Hello ${user} again!`}</h2>
    </>
  );
}

export default Component1;
```

> **優點：** Component2、3、4 完全不需要傳遞任何 props，程式碼更簡潔清晰。

---

## 7. 範例三：搭配 useState 更新 Context 值（本地狀態）

以下範例示範如何讓使用者透過表單更新使用者名稱。

> ⚠️ **設計限制說明：** 此範例中 `UpdateUsername` 使用 `useContext` 取得初始值後，將其存入**元件本地的 `useState`**。這意味著更新只影響該元件自身，**不會同步反映到其他使用同一 Context 的元件**（例如 `UserProfile`）。正確的共享更新模式請參閱 [第 8 節](#8-進階技巧透過-context-共享更新函式)。

```jsx
// UpdateUsername.jsx
import React, { useContext, useState } from "react";
import UserContext from "./MyUserContext";

function UpdateUsername() {
  const user = useContext(UserContext);          // 從 Context 取得初始值
  const [newUsername, setNewUsername] = useState("");  // 輸入框的暫存值
  const [username, setUsername] = useState(user);     // 目前顯示的使用者名稱

  // 處理輸入框變化
  const handleUsernameChange = (e) => {
    setNewUsername(e.target.value);
  };

  // 處理表單提交
  const handleSubmit = (e) => {
    e.preventDefault();            // 阻止表單預設提交行為
    if (newUsername) {
      console.log("Updating username to", newUsername);
      setUsername(newUsername);    // 更新顯示的使用者名稱
      setNewUsername("");          // 清空輸入框
    }
  };

  return (
    <div>
      <h2>Update Username</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          value={newUsername}
          onChange={handleUsernameChange}
          placeholder="Enter new username"
        />
        <button type="submit">Update</button>
      </form>
      <p>Current Username: {username}</p>
    </div>
  );
}

export default UpdateUsername;
```

### 狀態說明

| 狀態變數 | 初始值 | 用途 |
|---------|--------|------|
| `user` | 來自 Context（`"John Doe"`） | 取得 Context 中的原始值 |
| `newUsername` | `""` | 追蹤輸入框中的文字 |
| `username` | 來自 `user` | 顯示目前的使用者名稱（可更新） |

---

## 8. 進階技巧：透過 Context 共享更新函式

實務上，僅傳遞靜態值給 Context 通常不夠用。**正確做法**是將「值」與「更新函式」同時封裝在 `value` 物件中傳入 Provider，讓所有子元件都能讀取並觸發更新，實現真正的跨元件狀態同步。

### 8.1 正確模式：將 setter 傳入 Context

```jsx
import { useState, createContext, useContext } from "react";

const UserContext = createContext();

function UserApp() {
  const [user, setUser] = useState("John Doe");

  return (
    // 同時傳遞「值」與「更新函式」
    <UserContext.Provider value={{ user, setUser }}>
      <UserProfile />
      <UpdateUsername />
    </UserContext.Provider>
  );
}
```

子元件透過解構同時取得值與 setter：

```jsx
function UserProfile() {
  const { user } = useContext(UserContext);
  return (
    <div>
      <h1>User Profile</h1>
      <p>Username: {user}</p>
    </div>
  );
}

function UpdateUsername() {
  const { user, setUser } = useContext(UserContext);
  const [input, setInput] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    if (input) {
      setUser(input);   // 直接更新 Provider 的 value，所有消費者同步刷新
      setInput("");
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        value={input}
        onChange={(e) => setInput(e.target.value)}
        placeholder="Enter new username"
      />
      <button type="submit">Update</button>
      <p>Current: {user}</p>
    </form>
  );
}
```

> **關鍵差異：** `setUser` 直接來自 `UserApp` 的 `useState`。呼叫後 Provider 的 `value` 更新，`UserProfile` 與 `UpdateUsername` 都會同步收到新值。

---

### 8.2 最佳實踐：自定義 Hook 封裝

將 `useContext` 包裝為自定義 Hook 是業界推薦的模式，兼具語意清晰與錯誤防護：

```js
// MyUserContext.js
import { createContext, useContext, useState } from "react";

const UserContext = createContext();

// 自定義 Hook：語意化且有防呆機制
export function useUser() {
  const context = useContext(UserContext);
  if (context === undefined) {
    throw new Error("useUser 必須在 <UserContext.Provider> 內使用");
  }
  return context;
}

export default UserContext;
```

使用時直接呼叫 `useUser()`，更加語意化：

```jsx
function UserProfile() {
  const { user } = useUser();   // 取代 useContext(UserContext)
  return <p>Username: {user}</p>;
}

function UpdateUsername() {
  const { user, setUser } = useUser();
  // ...
}
```

**自定義 Hook 的優點：**
- 隱藏 Context 實作細節，元件不需要 `import UserContext`
- 若忘記加 Provider，會拋出清楚的錯誤訊息而非靜默失敗
- 未來更換狀態管理方案時只需修改 Hook，消費者元件不需異動

---

## 9. 完整綜合範例

以下是整合所有檔案的完整架構：

### 檔案結構
```
src/
  ├── MyUserContext.js     ← 建立 Context
  ├── UserApp.jsx          ← 根元件，提供 Provider
  ├── UserProfile.jsx      ← 讀取 Context（Consumer 方式）
  └── UpdateUsername.jsx   ← 讀取並更新（useContext 方式）
```

### MyUserContext.js
```js
import { createContext } from "react";

const UserContext = createContext();

export default UserContext;
```

### UserApp.jsx
```jsx
import React from "react";
import UserContext from "./MyUserContext";
import UserProfile from "./UserProfile";
import UpdateUsername from "./UpdateUsername";

function UserApp() {
  return (
    <UserContext.Provider value="John Doe">
      <UserProfile />
      <UpdateUsername />
    </UserContext.Provider>
  );
}

export default UserApp;
```

### UserProfile.jsx（Consumer 方式）
```jsx
import React from "react";
import UserContext from "./MyUserContext";

function UserProfile() {
  return (
    <UserContext.Consumer>
      {(username) => (
        <div>
          <h1>User Profile</h1>
          <p>Username: {username}</p>
        </div>
      )}
    </UserContext.Consumer>
  );
}

export default UserProfile;
```

### UpdateUsername.jsx（useContext 方式）
```jsx
import React, { useContext, useState } from "react";
import UserContext from "./MyUserContext";

function UpdateUsername() {
  const user = useContext(UserContext);
  const [newUsername, setNewUsername] = useState("");
  const [username, setUsername] = useState(user);

  const handleUsernameChange = (e) => {
    setNewUsername(e.target.value);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (newUsername) {
      setUsername(newUsername);
      setNewUsername("");
    }
  };

  return (
    <div>
      <h2>Update Username</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          value={newUsername}
          onChange={handleUsernameChange}
          placeholder="Enter new username"
        />
        <button type="submit">Update</button>
      </form>
      <p>Current Username: {username}</p>
    </div>
  );
}

export default UpdateUsername;
```

---

## 10. 重點整理

### React Context 使用步驟

```
步驟 1：建立 Context
  const MyContext = createContext();           // 或 createContext(預設值)

步驟 2：用 Provider 包裹元件並提供值
  <MyContext.Provider value={{ data, setData }}>  // 同時傳值與 setter
    <子元件 />
  </MyContext.Provider>

步驟 3：在子元件中讀取 Context 值
  方法 A（推薦）：const { data, setData } = useContext(MyContext);
  方法 B（最佳實踐）：const { data, setData } = useMyContext();  // 自定義 Hook
  方法 C（舊式）：<MyContext.Consumer>{(value) => <div>{value}</div>}</MyContext.Consumer>
```

### 三種讀取方式比較

| 方式 | 語法 | 適用場景 |
|------|------|---------|
| `useContext` Hook | `const value = useContext(MyContext)` | 一般用途，程式碼簡潔 |
| 自定義 Hook | `const value = useMyContext()` | 推薦，語意化且有防呆保護 |
| `Consumer` 元件 | `<MyContext.Consumer>{v => ...}</MyContext.Consumer>` | 需相容舊版類別元件時使用 |

### 常見錯誤

| 錯誤 | 問題說明 | 正確做法 |
|------|---------|---------|
| Context 更新不同步 | 用 `useState(contextValue)` 快取初始值，更新只影響本地 | 直接使用 `useContext` 取值，setter 透過 Context 傳遞 |
| 忘記包裹 Provider | 元件取得 `undefined` 或預設值，行為異常 | 確保消費元件被 Provider 祖先包裹 |
| 傳遞未穩定的物件 | `value={{ user, setUser }}` 每次渲染產生新物件，觸發不必要重渲 | 用 `useMemo` 穩定 value，或拆分 Context |
| 將 Context 當作萬能解法 | 所有狀態都放進 Context，每次更新都重渲大範圍元件 | 只放真正需要跨層共享的資料 |

### 效能注意事項

當 `Provider` 的 `value` 更新時，**所有**消費該 Context 的元件都會重新渲染。為避免效能問題：

```jsx
// ❌ 每次 UserApp 渲染都產生新物件，導致所有消費者重渲
<UserContext.Provider value={{ user, setUser }}>

// ✅ 用 useMemo 穩定參考，只在 user 真正改變時才更新
const contextValue = useMemo(() => ({ user, setUser }), [user]);
<UserContext.Provider value={contextValue}>
```

亦可將「常變動的值」與「不常變動的 setter」拆分成兩個 Context，讓只需要 setter 的元件不因值的變動而重渲。

### 何時使用 Context？

✅ **適合使用：**
- 使用者登入資訊（user profile）
- 應用主題（dark/light mode）
- 語言 / 地區設定
- 需要跨多層元件共享的資料

❌ **不適合使用：**
- 只在父子元件之間傳遞的資料（直接用 `props` 即可）
- 高頻更新的資料（如動畫狀態、滾動位置，考慮使用 Zustand / Jotai 等狀態庫）
