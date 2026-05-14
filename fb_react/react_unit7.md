# Unit 7 — UI 樣式整合（Styling）

> **學習目標**：完成本單元後，你能選擇適合的樣式方案，用 CSS Modules 避免命名衝突、用 Tailwind CSS 快速建構響應式介面、並整合主流 UI 元件庫加速開發。  
> **預估時間**：5–7 小時  
> **程度**：有基礎（需完成 Unit 1–3）

---

## 三種方案比較

| 方案 | 定位 | 適用情境 |
|------|------|---------|
| **CSS Modules** | Scoped CSS | 想保留手寫 CSS，但避免全域汙染 |
| **Tailwind CSS** | Utility-first | 快速開發、一致的設計系統 |
| **UI 元件庫（MUI/AntD）** | 預建元件 | 快速完成管理後台、表單介面 |

---

## 7.1 CSS Modules

### 概念說明
CSS Modules 讓每個 CSS 檔案都有獨立作用域（Scoped Styles）。同名 class 不同元件不會互相干擾，因為構建工具會自動把 class 名稱加上雜湊後綴。

```
傳統 CSS 問題：                CSS Modules 解決方式：
.button { ... }               .button → .Button_button__x3k2a
全域汙染，所有 .button 都受影響  每個元件獨立作用域
```

### 語法結構

```
Button.module.css   ← 命名慣例：檔名加 .module.css
Button.jsx          ← 對應元件
```

### 基本用法

```css
/* components/Button/Button.module.css */
.button {
  padding: 8px 16px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: opacity 0.2s;
}

.button:hover {
  opacity: 0.85;
}

/* 變體（Variants） */
.primary {
  background-color: #3b82f6;
  color: white;
}

.secondary {
  background-color: #e5e7eb;
  color: #374151;
}

.danger {
  background-color: #ef4444;
  color: white;
}

/* 尺寸 */
.small {
  padding: 4px 10px;
  font-size: 12px;
}

.large {
  padding: 12px 24px;
  font-size: 16px;
}
```

```jsx
// components/Button/Button.jsx
import styles from './Button.module.css';

function Button({
  children,
  variant = 'primary',
  size = 'medium',
  disabled = false,
  onClick,
}) {
  // 組合多個 class 名稱
  const className = [
    styles.button,
    styles[variant],     // styles.primary / styles.secondary / styles.danger
    size !== 'medium' ? styles[size] : '',
  ].filter(Boolean).join(' ');

  return (
    <button className={className} disabled={disabled} onClick={onClick}>
      {children}
    </button>
  );
}

export default Button;
```

```jsx
// 使用
<Button variant="primary" onClick={handleSubmit}>送出</Button>
<Button variant="secondary">取消</Button>
<Button variant="danger" size="small">刪除</Button>
```

---

### 組合多個 class — `clsx` 套件

手動組合 class 名稱很繁瑣，`clsx` 讓這件事更簡潔：

```bash
npm install clsx
```

```jsx
import styles from './Button.module.css';
import clsx from 'clsx';

function Button({ variant = 'primary', size, disabled, children, onClick }) {
  return (
    <button
      className={clsx(
        styles.button,
        styles[variant],
        size && styles[size],
        disabled && styles.disabled,  // 條件式 class
      )}
      disabled={disabled}
      onClick={onClick}
    >
      {children}
    </button>
  );
}
```

---

### 全域樣式

```css
/* Button.module.css */

/* 只有特定 class 需要全域作用域時，用 :global */
:global(.ant-btn) {
  border-radius: 4px;
}

/* 元件內部的全域 class */
.wrapper :global(.icon) {
  margin-right: 8px;
}
```

```css
/* 全域樣式放在 index.css 或 global.css（不加 .module） */
/* src/index.css */
*, *::before, *::after {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: 'Inter', sans-serif;
  background-color: #f9fafb;
}
```

#### ⚠️ 常見錯誤

```jsx
// ❌ 錯誤：忘記用 styles. 前綴，直接用字串
<div className="container">  {/* 這是全域 class！ */}

// ✅ 正確：透過 styles 物件引用
<div className={styles.container}>

// ❌ 錯誤：動態 class 用字串拼接
<div className={`styles.button styles.${variant}`}>  {/* 無法正常運作 */}

// ✅ 正確：用物件的屬性存取
<div className={clsx(styles.button, styles[variant])}>
```

> **現在試試看**：建立一個 `Card.module.css`，設計一個包含 `.card`、`.cardHeader`、`.cardBody` 的卡片元件，並用 `clsx` 實作 `elevated`（有陰影）和 `flat`（無陰影）兩種變體。

---

## 7.2 Tailwind CSS

### 概念說明
Tailwind 採用 **Utility-first**（功能優先）的概念：不寫自訂 CSS，而是把大量小型的 utility class 直接寫在 HTML/JSX 中。

```
傳統寫法：                         Tailwind 寫法：
.card {                            <div className="rounded-lg
  border-radius: 8px;                bg-white
  background: white;                 shadow-md
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);  p-6">
  padding: 24px;
}
```

### 安裝（Vite 專案）

```bash
npm install -D tailwindcss @tailwindcss/vite
```

```js
// vite.config.js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
})
```

```css
/* src/index.css */
@import "tailwindcss";
```

---

### 核心概念

#### 間距（Spacing）— `p-`, `m-`, `gap-`

```jsx
// p = padding, m = margin, 數字對應 4px 的倍數
<div className="p-4">          {/* padding: 16px */}
<div className="px-6 py-3">   {/* padding-x: 24px, padding-y: 12px */}
<div className="mt-8 mb-4">   {/* margin-top: 32px, margin-bottom: 16px */}
<div className="gap-4">       {/* gap: 16px（用於 flex/grid） */}
```

#### 排版（Layout）— Flexbox / Grid

```jsx
{/* Flexbox */}
<div className="flex items-center justify-between gap-4">
  <span>左側</span>
  <span>右側</span>
</div>

{/* Grid */}
<div className="grid grid-cols-3 gap-6">
  <div>欄1</div>
  <div>欄2</div>
  <div>欄3</div>
</div>

{/* 常用 flex 組合 */}
<div className="flex flex-col min-h-screen">   {/* 垂直全高 */}
  <header className="flex-shrink-0">...</header>
  <main className="flex-1">...</main>          {/* 佔滿剩餘空間 */}
  <footer className="flex-shrink-0">...</footer>
</div>
```

#### 顏色與外觀

```jsx
{/* 顏色：色系-深淺（50-950） */}
<div className="bg-blue-500 text-white">       {/* 藍色背景白色文字 */}
<div className="bg-gray-100 text-gray-800">   {/* 灰色背景深灰文字 */}
<button className="bg-red-500 hover:bg-red-600"> {/* 懸停變色 */}

{/* 邊框 */}
<div className="border border-gray-200 rounded-lg">
<div className="border-2 border-blue-500 rounded-full">

{/* 陰影 */}
<div className="shadow-sm">   {/* 淺陰影 */}
<div className="shadow-md">   {/* 中等陰影 */}
<div className="shadow-xl">   {/* 深陰影 */}
```

---

### 響應式設計（Responsive Design）

Tailwind 採用 **Mobile First** 策略：沒有前綴 = 手機版，前綴 = 指定斷點以上。

| 前綴 | 最小寬度 | 說明 |
|------|---------|------|
| （無） | 0px | 所有尺寸 |
| `sm:` | 640px | 小螢幕以上 |
| `md:` | 768px | 中螢幕以上（平板） |
| `lg:` | 1024px | 大螢幕以上（桌機） |
| `xl:` | 1280px | 超大螢幕 |

```jsx
{/* 手機單欄 → 平板雙欄 → 桌機三欄 */}
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
  {products.map(p => <ProductCard key={p.id} product={p} />)}
</div>

{/* 手機隱藏，桌機顯示 */}
<aside className="hidden lg:block w-64">側邊欄</aside>

{/* 文字大小響應式 */}
<h1 className="text-2xl md:text-4xl lg:text-5xl font-bold">
  標題
</h1>
```

---

### 常用元件範例

```jsx
// 導覽列（Navbar）
function Navbar() {
  return (
    <nav className="bg-white border-b border-gray-200 px-6 py-4">
      <div className="max-w-7xl mx-auto flex items-center justify-between">
        <span className="text-xl font-bold text-blue-600">Logo</span>
        <div className="hidden md:flex items-center gap-6">
          <a href="#" className="text-gray-600 hover:text-blue-600 transition-colors">首頁</a>
          <a href="#" className="text-gray-600 hover:text-blue-600 transition-colors">關於</a>
          <button className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg transition-colors">
            登入
          </button>
        </div>
      </div>
    </nav>
  );
}

// 卡片（Card）
function ProductCard({ product }) {
  return (
    <div className="bg-white rounded-xl shadow-md overflow-hidden hover:shadow-lg transition-shadow">
      <img src={product.image} alt={product.name} className="w-full h-48 object-cover" />
      <div className="p-4">
        <h3 className="font-semibold text-gray-800 truncate">{product.name}</h3>
        <p className="text-gray-500 text-sm mt-1 line-clamp-2">{product.description}</p>
        <div className="flex items-center justify-between mt-4">
          <span className="text-blue-600 font-bold text-lg">${product.price}</span>
          <button className="bg-blue-500 hover:bg-blue-600 text-white text-sm px-3 py-1.5 rounded-lg transition-colors">
            加入購物車
          </button>
        </div>
      </div>
    </div>
  );
}
```

---

### 用 `cn()` 工具函式整合 clsx + Tailwind

```bash
npm install clsx tailwind-merge
```

```js
// lib/utils.js
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

// 這個組合讓你可以安全地覆蓋 Tailwind class
export function cn(...inputs) {
  return twMerge(clsx(inputs));
}
```

```jsx
// 有了 cn()，class 衝突時後面的優先
import { cn } from '../lib/utils';

function Button({ className, variant = 'primary', ...props }) {
  return (
    <button
      className={cn(
        'px-4 py-2 rounded-lg font-medium transition-colors',
        variant === 'primary' && 'bg-blue-500 hover:bg-blue-600 text-white',
        variant === 'secondary' && 'bg-gray-200 hover:bg-gray-300 text-gray-800',
        className,  // 允許外部覆蓋 class
      )}
      {...props}
    />
  );
}

// 使用者可以覆蓋樣式
<Button className="w-full text-lg">全寬大按鈕</Button>
```

#### ⚠️ 常見錯誤

```jsx
// ❌ 不要用字串插值動態建構 class（Tailwind 無法掃描，不會產生樣式）
const color = 'blue';
<div className={`bg-${color}-500`}>  {/* 無效！ */}

// ✅ 使用完整的 class 名稱（Tailwind 會在建構時掃描）
const colorClass = {
  blue: 'bg-blue-500',
  red: 'bg-red-500',
  green: 'bg-green-500',
};
<div className={colorClass[color]}>  {/* 有效 */}
```

> **現在試試看**：用 Tailwind CSS 建立一個響應式的「登入表單」，手機版全寬、桌機版置中最大寬度 400px，包含 Email 輸入框、密碼輸入框、登入按鈕。

---

## 7.3 UI 元件庫

### Material UI（MUI）

Google Material Design 的 React 實作，功能最完整，適合企業應用。

```bash
npm install @mui/material @emotion/react @emotion/styled
npm install @mui/icons-material  # 可選：圖示庫
```

```jsx
import {
  Button, TextField, Card, CardContent, CardActions,
  Typography, Box, Grid2 as Grid, CircularProgress,
  Alert, Chip, Avatar, IconButton, Tooltip,
} from '@mui/material';
import { Delete, Edit, Add } from '@mui/icons-material';

function UserCard({ user, onEdit, onDelete }) {
  return (
    <Card sx={{ maxWidth: 345 }}>
      <CardContent>
        <Box display="flex" alignItems="center" gap={2} mb={2}>
          <Avatar src={user.avatar}>{user.name[0]}</Avatar>
          <Box>
            <Typography variant="h6">{user.name}</Typography>
            <Typography variant="body2" color="text.secondary">{user.email}</Typography>
          </Box>
        </Box>
        <Box display="flex" gap={1}>
          <Chip label={user.role} color="primary" size="small" />
          <Chip label={user.status} color="success" size="small" />
        </Box>
      </CardContent>
      <CardActions>
        <Tooltip title="編輯">
          <IconButton color="primary" onClick={() => onEdit(user)}>
            <Edit />
          </IconButton>
        </Tooltip>
        <Tooltip title="刪除">
          <IconButton color="error" onClick={() => onDelete(user.id)}>
            <Delete />
          </IconButton>
        </Tooltip>
      </CardActions>
    </Card>
  );
}

// MUI 表單範例
function LoginForm() {
  return (
    <Box
      component="form"
      sx={{ display: 'flex', flexDirection: 'column', gap: 2, maxWidth: 400, mx: 'auto', mt: 8 }}
    >
      <Typography variant="h5" textAlign="center">登入</Typography>
      <TextField label="Email" type="email" required fullWidth />
      <TextField label="密碼" type="password" required fullWidth />
      <Button type="submit" variant="contained" size="large" fullWidth>
        登入
      </Button>
    </Box>
  );
}
```

---

### Ant Design（antd）

阿里巴巴出品，在亞洲企業中廣泛使用，中文文件完善。

```bash
npm install antd
```

```jsx
import { Table, Button, Form, Input, Modal, Space, Tag, Popconfirm, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';

// antd Table 範例
const columns = [
  { title: '姓名', dataIndex: 'name', key: 'name' },
  { title: 'Email', dataIndex: 'email', key: 'email' },
  {
    title: '角色',
    dataIndex: 'role',
    key: 'role',
    render: (role) => (
      <Tag color={role === 'admin' ? 'red' : 'blue'}>{role}</Tag>
    ),
  },
  {
    title: '操作',
    key: 'actions',
    render: (_, record) => (
      <Space>
        <Button icon={<EditOutlined />} onClick={() => handleEdit(record)}>編輯</Button>
        <Popconfirm
          title="確定刪除？"
          onConfirm={() => handleDelete(record.id)}
          okText="確定"
          cancelText="取消"
        >
          <Button danger icon={<DeleteOutlined />}>刪除</Button>
        </Popconfirm>
      </Space>
    ),
  },
];

function UserTable({ users }) {
  return (
    <>
      <Button type="primary" icon={<PlusOutlined />} style={{ marginBottom: 16 }}>
        新增使用者
      </Button>
      <Table columns={columns} dataSource={users} rowKey="id" />
    </>
  );
}

// antd Form 範例
function CreateUserModal({ open, onClose, onSubmit }) {
  const [form] = Form.useForm();

  const handleFinish = async (values) => {
    await onSubmit(values);
    form.resetFields();
    onClose();
    message.success('使用者建立成功！');
  };

  return (
    <Modal title="新增使用者" open={open} onCancel={onClose} footer={null}>
      <Form form={form} layout="vertical" onFinish={handleFinish}>
        <Form.Item label="姓名" name="name" rules={[{ required: true, message: '請輸入姓名' }]}>
          <Input />
        </Form.Item>
        <Form.Item
          label="Email"
          name="email"
          rules={[
            { required: true, message: '請輸入 Email' },
            { type: 'email', message: 'Email 格式不正確' },
          ]}
        >
          <Input />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" block>建立</Button>
        </Form.Item>
      </Form>
    </Modal>
  );
}
```

---

### shadcn/ui

不是傳統套件，而是把元件「複製」到你的專案中，完全可自訂，搭配 Tailwind CSS 使用。

```bash
npx shadcn@latest init
npx shadcn@latest add button card input label form
```

```jsx
// shadcn 元件直接在你的 src/components/ui/ 中，可以自由修改！
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';

function LoginCard() {
  return (
    <Card className="w-[400px] mx-auto mt-20">
      <CardHeader>
        <CardTitle>歡迎回來</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-1">
          <Label htmlFor="email">Email</Label>
          <Input id="email" type="email" placeholder="name@example.com" />
        </div>
        <div className="space-y-1">
          <Label htmlFor="password">密碼</Label>
          <Input id="password" type="password" />
        </div>
      </CardContent>
      <CardFooter>
        <Button className="w-full">登入</Button>
      </CardFooter>
    </Card>
  );
}
```

| 元件庫 | 優點 | 缺點 | 適合情境 |
|--------|------|------|---------|
| MUI | 功能最完整，文件豐富 | 樣式較難覆蓋，bundle 較大 | 企業後台，需要現成複雜元件 |
| Ant Design | 中文文件好，Table 強 | 設計風格固定，客製化複雜 | 管理系統，亞洲市場 |
| shadcn/ui | 完全可控，Tailwind 整合好 | 需要自己組合邏輯 | 現代設計風格，自訂需求高 |

---

## 綜合實作練習

**目標**：建立一個產品管理頁面，使用 Tailwind CSS 或 shadcn/ui，包含：
1. 響應式頁首（Logo + 導覽選單）
2. 產品卡片格格（手機1欄 → 桌機3欄）
3. 每張卡片包含圖片、名稱、價格、「加入購物車」按鈕
4. 「新增產品」按鈕，點擊後顯示表單對話框（Modal）
5. 表單有名稱、價格、描述三個欄位

**延伸挑戰**：加入暗黑模式（Dark Mode）切換按鈕。

---

## 單元小測驗

1. CSS Modules 如何防止 class 名稱衝突？
2. Tailwind 的 `md:grid-cols-2` 表示什麼意思？
3. 為什麼不能在 Tailwind 中用 `` `bg-${color}-500` `` 這樣的字串插值？
4. `tailwind-merge` 解決了什麼問題？和 `clsx` 有何不同？
5. shadcn/ui 和 MUI 最大的使用方式差異是什麼？

---

## 里程碑 ✅

- [ ] 能用 CSS Modules 建立有多個變體的元件，搭配 `clsx` 組合 class
- [ ] 能安裝 Tailwind CSS 並用 utility class 建立響應式卡片佈局
- [ ] 能正確使用響應式前綴（`sm:` `md:` `lg:`）實作 Mobile First 設計
- [ ] 能整合至少一種 UI 元件庫（MUI 或 shadcn/ui）
- [ ] 完成產品管理頁面綜合實作練習
