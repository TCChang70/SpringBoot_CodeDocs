const arr1 = [1, 2, 3];
const arr2 = [4, 5, 6];

// 合併陣列
const combined = [...arr1, ...arr2];
console.log(combined); // [1, 2, 3, 4, 5, 6]

// 複製陣列（淺拷貝）
const copy = [...arr1];

// 新增元素到陣列（React state 更新常見模式）
const newArr = [...arr1, 4];
console.log(newArr); // [1, 2, 3, 4]