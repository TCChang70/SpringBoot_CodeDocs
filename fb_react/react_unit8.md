# Unit 8 — 表單處理（Form Handling）

> **學習目標**：完成本單元後，你能用原生受控表單、React Hook Form、以及 Zod 驗證建立完整且可維護的表單，包含即時驗證與錯誤訊息顯示。  
> **預估時間**：5–6 小時  
> **程度**：有基礎（需完成 Unit 1–3）

---

## 三種表單方案比較

| 方案 | 適用情境 | 優點 | 缺點 |
|------|---------|------|------|
| **受控表單（Controlled）** | 簡單表單（1–3 個欄位） | 無需安裝，React 原生 | 欄位多時程式碼冗長 |
| **React Hook Form** | 中大型表單 | 效能佳（非受控），API 簡潔 | 需學習 API |
| **RHF + Zod** | 需嚴謹驗證的表單 | 型別安全，驗證邏輯集中 | 學習曲線較高 |

---

## 8.1 受控表單（Controlled Form）

### 概念說明
受控元件（Controlled Component）：React 的 `state` 是表單的唯一資料來源。每次輸入都呼叫 `onChange` 更新 state，渲染出的值始終與 state 一致。

```
使用者輸入 → onChange 事件 → setState → 重新渲染 → input 顯示新值
```

### 單一欄位

```jsx
import { useState } from 'react';

function SimpleInput() {
  const [name, setName] = useState('');

  return (
    <div>
      <input
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="輸入你的名字"
      />
      <p>你好，{name || '陌生人'}！</p>
    </div>
  );
}
```

---

### 多欄位統一管理

```jsx
import { useState } from 'react';

function RegisterForm() {
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  const [errors, setErrors] = useState({});

  // 統一 onChange 處理器（用 name 屬性識別欄位）
  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    // 輸入時清除該欄位的錯誤
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
  };

  const validate = () => {
    const newErrors = {};
    if (!form.username.trim()) newErrors.username = '使用者名稱不能為空';
    else if (form.username.length < 3) newErrors.username = '至少需要 3 個字元';

    if (!form.email) newErrors.email = 'Email 不能為空';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      newErrors.email = 'Email 格式不正確';
    }

    if (!form.password) newErrors.password = '密碼不能為空';
    else if (form.password.length < 8) newErrors.password = '密碼至少 8 個字元';

    if (form.password !== form.confirmPassword) {
      newErrors.confirmPassword = '兩次密碼不一致';
    }

    return newErrors;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const newErrors = validate();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
    console.log('表單送出：', form);
    // 送出到 API...
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <div>
        <label htmlFor="username">使用者名稱</label>
        <input
          id="username"
          name="username"
          value={form.username}
          onChange={handleChange}
        />
        {errors.username && <span className="error">{errors.username}</span>}
      </div>

      <div>
        <label htmlFor="email">Email</label>
        <input
          id="email"
          name="email"
          type="email"
          value={form.email}
          onChange={handleChange}
        />
        {errors.email && <span className="error">{errors.email}</span>}
      </div>

      <div>
        <label htmlFor="password">密碼</label>
        <input
          id="password"
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
        />
        {errors.password && <span className="error">{errors.password}</span>}
      </div>

      <div>
        <label htmlFor="confirmPassword">確認密碼</label>
        <input
          id="confirmPassword"
          name="confirmPassword"
          type="password"
          value={form.confirmPassword}
          onChange={handleChange}
        />
        {errors.confirmPassword && <span className="error">{errors.confirmPassword}</span>}
      </div>

      <button type="submit">註冊</button>
    </form>
  );
}
```

#### ⚠️ 常見錯誤

```jsx
// ❌ 直接修改 state 物件
const handleChange = (e) => {
  form.username = e.target.value;  // 不觸發重渲染！
  setForm(form);
};

// ✅ 展開並產生新物件
const handleChange = (e) => {
  setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
};

// ❌ submit 沒有 e.preventDefault()（頁面會重新整理）
const handleSubmit = () => { /* ... */ };

// ✅ 阻止預設行為
const handleSubmit = (e) => {
  e.preventDefault();
  /* ... */
};
```

> **現在試試看**：在上面的 `RegisterForm` 中加入「顯示/隱藏密碼」切換按鈕，並在送出時顯示「正在送出中...」的 loading 狀態。

---

## 8.2 React Hook Form

### 概念說明
React Hook Form（RHF）採用**非受控元件**策略，直接操作 DOM，而非每次輸入都更新 React state，大幅減少重新渲染次數，提升效能。

```
受控表單：每次輸入都重新渲染
React Hook Form：只在送出或特定事件才觸發渲染
```

### 安裝

```bash
npm install react-hook-form
```

---

### 基本用法

```jsx
import { useForm } from 'react-hook-form';

function LoginForm() {
  const {
    register,          // 將 input 註冊到 form
    handleSubmit,      // 送出前自動驗證
    formState: { errors, isSubmitting },  // 表單狀態
    reset,             // 重置表單
  } = useForm();

  const onSubmit = async (data) => {
    // data 是通過驗證後的表單資料物件
    console.log(data); // { email: 'test@example.com', password: '123456' }
    await loginApi(data);
    reset();
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div>
        <label>Email</label>
        <input
          type="email"
          {...register('email', {
            required: 'Email 不能為空',
            pattern: {
              value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
              message: 'Email 格式不正確',
            },
          })}
        />
        {errors.email && <span>{errors.email.message}</span>}
      </div>

      <div>
        <label>密碼</label>
        <input
          type="password"
          {...register('password', {
            required: '密碼不能為空',
            minLength: { value: 8, message: '密碼至少 8 個字元' },
          })}
        />
        {errors.password && <span>{errors.password.message}</span>}
      </div>

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? '登入中...' : '登入'}
      </button>
    </form>
  );
}
```

---

### 完整驗證規則

```jsx
const { register, formState: { errors } } = useForm();

// register 的驗證選項
{...register('fieldName', {
  required: '這個欄位必填',              // 必填
  minLength: { value: 3, message: '至少 3 字元' },
  maxLength: { value: 50, message: '最多 50 字元' },
  min: { value: 0, message: '不能小於 0' },   // 數字最小值
  max: { value: 100, message: '不能大於 100' },
  pattern: {
    value: /^[a-zA-Z0-9]+$/,
    message: '只能包含英文和數字',
  },
  validate: (value) => {
    // 自訂驗證函式，回傳 true 表示通過，字串表示錯誤訊息
    if (value.includes(' ')) return '不能包含空格';
    return true;
  },
  // 多個自訂驗證
  validate: {
    noSpace: (v) => !v.includes(' ') || '不能包含空格',
    notAdmin: (v) => v !== 'admin' || '不能使用 admin 作為名稱',
  },
})}
```

---

### `watch` — 監聽欄位值

```jsx
function RegisterForm() {
  const { register, handleSubmit, watch, formState: { errors } } = useForm();

  const password = watch('password');  // 即時監聽 password 欄位的值

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <input type="password" {...register('password', { required: true, minLength: 8 })} />

      <input
        type="password"
        {...register('confirmPassword', {
          validate: (value) => value === password || '兩次密碼不一致',
        })}
      />
      {errors.confirmPassword && <span>{errors.confirmPassword.message}</span>}
    </form>
  );
}
```

---

### `setValue` / `getValues` — 程式化操作

```jsx
function EditUserForm({ user }) {
  const { register, handleSubmit, setValue, getValues } = useForm({
    defaultValues: {
      name: user.name,
      email: user.email,
    },
  });

  // 程式化設定欄位值（例如：從 API 載入後填入）
  useEffect(() => {
    setValue('name', user.name);
    setValue('email', user.email);
  }, [user, setValue]);

  // 讀取所有目前的值
  const handlePreview = () => {
    const values = getValues();
    console.log(values);
  };

  return <form onSubmit={handleSubmit(onSubmit)}>...</form>;
}
```

---

### 動態欄位（Field Array）— `useFieldArray`

```jsx
import { useForm, useFieldArray } from 'react-hook-form';

function MultiEmailForm() {
  const { register, handleSubmit, control } = useForm({
    defaultValues: { emails: [{ value: '' }] },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'emails',
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      {fields.map((field, index) => (
        <div key={field.id}>
          <input
            {...register(`emails.${index}.value`, {
              required: 'Email 不能為空',
            })}
            placeholder={`Email ${index + 1}`}
          />
          <button type="button" onClick={() => remove(index)}>移除</button>
        </div>
      ))}
      <button type="button" onClick={() => append({ value: '' })}>
        新增 Email
      </button>
      <button type="submit">送出</button>
    </form>
  );
}
```

---

## 8.3 Zod 結構驗證

### 概念說明
Zod 讓你用 JavaScript 定義資料結構（Schema），然後用它來驗證資料格式。與 React Hook Form 整合後，驗證邏輯集中在一個地方，更易維護。

```
Schema 定義：     輸入資料       驗證結果
registerSchema → { name, email } → ✅ 通過 / ❌ 錯誤訊息
```

### 安裝

```bash
npm install zod @hookform/resolvers
```

---

### Schema 定義

```js
// schemas/userSchema.js
import { z } from 'zod';

export const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'Email 不能為空')
    .email('Email 格式不正確'),
  password: z
    .string()
    .min(1, '密碼不能為空')
    .min(8, '密碼至少 8 個字元'),
});

export const registerSchema = z.object({
  username: z
    .string()
    .min(1, '使用者名稱不能為空')
    .min(3, '至少需要 3 個字元')
    .max(20, '最多 20 個字元')
    .regex(/^[a-zA-Z0-9_]+$/, '只能包含英文、數字、底線'),

  email: z
    .string()
    .min(1, 'Email 不能為空')
    .email('Email 格式不正確'),

  password: z
    .string()
    .min(8, '密碼至少 8 個字元')
    .regex(/[A-Z]/, '需要包含至少一個大寫字母')
    .regex(/[0-9]/, '需要包含至少一個數字'),

  confirmPassword: z.string().min(1, '請確認密碼'),

  age: z
    .number({ invalid_type_error: '請輸入數字' })
    .int('請輸入整數')
    .min(18, '必須年滿 18 歲')
    .max(120, '年齡不合理'),

  role: z.enum(['user', 'editor', 'admin'], {
    errorMap: () => ({ message: '請選擇有效的角色' }),
  }),

  website: z
    .string()
    .url('請輸入有效的網址')
    .optional(),         // 可選欄位

  bio: z.string().max(200, '簡介最多 200 字').nullable(),
})
// 跨欄位驗證（confirmPassword 必須等於 password）
.refine((data) => data.password === data.confirmPassword, {
  message: '兩次密碼不一致',
  path: ['confirmPassword'],  // 錯誤顯示在 confirmPassword 欄位
});

// 從 Schema 推導 TypeScript 型別（TypeScript 專案用）
// export type RegisterFormData = z.infer<typeof registerSchema>;
```

---

### 與 React Hook Form 整合

```jsx
// forms/RegisterForm.jsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { registerSchema } from '../schemas/userSchema';

function RegisterForm() {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting, isSubmitSuccessful },
    reset,
  } = useForm({
    resolver: zodResolver(registerSchema),  // 用 Zod 取代 RHF 內建驗證
    defaultValues: {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      role: 'user',
    },
  });

  const onSubmit = async (data) => {
    try {
      await registerApi(data);
      reset();
    } catch (error) {
      console.error('註冊失敗：', error);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      {/* 使用者名稱 */}
      <div>
        <label htmlFor="username">使用者名稱</label>
        <input id="username" {...register('username')} />
        {errors.username && (
          <p className="error">{errors.username.message}</p>
        )}
      </div>

      {/* Email */}
      <div>
        <label htmlFor="email">Email</label>
        <input id="email" type="email" {...register('email')} />
        {errors.email && (
          <p className="error">{errors.email.message}</p>
        )}
      </div>

      {/* 密碼 */}
      <div>
        <label htmlFor="password">密碼</label>
        <input id="password" type="password" {...register('password')} />
        {errors.password && (
          <p className="error">{errors.password.message}</p>
        )}
      </div>

      {/* 確認密碼 */}
      <div>
        <label htmlFor="confirmPassword">確認密碼</label>
        <input id="confirmPassword" type="password" {...register('confirmPassword')} />
        {errors.confirmPassword && (
          <p className="error">{errors.confirmPassword.message}</p>
        )}
      </div>

      {/* 角色下拉選單 */}
      <div>
        <label htmlFor="role">角色</label>
        <select id="role" {...register('role')}>
          <option value="user">一般使用者</option>
          <option value="editor">編輯者</option>
          <option value="admin">管理員</option>
        </select>
        {errors.role && <p className="error">{errors.role.message}</p>}
      </div>

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? '註冊中...' : '立即註冊'}
      </button>

      {isSubmitSuccessful && <p className="success">註冊成功！</p>}
    </form>
  );
}
```

---

### 可重用的 FormField 元件

```jsx
// components/FormField.jsx — 提取重複的 label + input + error 模式
function FormField({ label, name, register, error, type = 'text', ...rest }) {
  return (
    <div className="form-field">
      <label htmlFor={name}>{label}</label>
      <input
        id={name}
        type={type}
        {...register(name)}
        {...rest}
        aria-invalid={!!error}
        aria-describedby={error ? `${name}-error` : undefined}
      />
      {error && (
        <p id={`${name}-error`} role="alert" className="error">
          {error.message}
        </p>
      )}
    </div>
  );
}

// 使用
<FormField
  label="Email"
  name="email"
  type="email"
  register={register}
  error={errors.email}
  placeholder="your@email.com"
/>
```

---

## 綜合實作練習

**目標**：建立一個「個人資料編輯表單」，使用 React Hook Form + Zod，包含：

1. **欄位**：姓名、Email、生日（`<input type="date">`）、個人簡介（`<textarea>`）、個人網站（選填）、性別（Radio）、接收電子報（Checkbox）
2. **驗證規則（用 Zod 定義）**：
   - 姓名：必填，2–30 字元
   - Email：必填，正確格式
   - 生日：必填，日期必須是過去
   - 個人簡介：最多 200 字
   - 個人網站：選填，如果填了必須是有效 URL
3. **UX**：
   - 即時顯示各欄位的錯誤訊息
   - 送出按鈕在表單驗證通過前呈現 disabled
   - 顯示「剩餘字數」計數器（個人簡介欄位）
   - 送出中顯示 loading 狀態
4. **初始值**：從 API 載入用戶資料後，用 `reset()` 或 `defaultValues` 填入表單

---

## 單元小測驗

1. 受控元件和非受控元件最根本的差異是什麼？
2. React Hook Form 為什麼比受控表單效能更好？
3. `watch` 和 `getValues` 的差異是什麼？
4. Zod 的 `.refine()` 用在什麼情境？
5. `@hookform/resolvers` 的作用是什麼？

---

## 里程碑 ✅

- [ ] 能用 `useState` 建立多欄位受控表單，含手動驗證邏輯
- [ ] 能安裝並使用 React Hook Form，用 `register` 連結欄位
- [ ] 能用 `watch` 實作跨欄位驗證（如：確認密碼）
- [ ] 能用 Zod 定義 Schema 並透過 `zodResolver` 整合到 RHF
- [ ] 能提取可重用的 `FormField` 元件，減少重複程式碼
- [ ] 完成個人資料編輯表單綜合實作練習
