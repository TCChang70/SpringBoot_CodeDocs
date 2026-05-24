// 箭頭函式 — 完整寫法
// const add = (a, b) => {
//   return a + b;
// };

// 箭頭函式 — 單行簡化（省略 return 和大括號）
const add = (a, b) => a + b;

// 只有一個參數時，括號可省略
const double = x => x * 2;

// 沒有參數時，括號不可省略
const greet = () => "Hello, React!";

console.log(add(3, 5));   // 8
console.log(double(4));   // 8
console.log(greet());     // Hello, React!