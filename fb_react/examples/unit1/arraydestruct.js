
const colors = ["red", "green", "blue"];

const [first, second] = colors;
console.log(first);  // "red"
console.log(second); // "green"

// 跳過元素
const [, , third] = colors;
console.log(third);  // "blue"

// React useState 就是用陣列解構！
// const [count, setCount] = useState(0);
// console.log(count); // 0
// setCount(1);
// console.log(count); // 1    
const product = { id: 1, title: "iPhone", price: 999 };
const { title, price } = product;
console.log(title); // "iPhone"
console.log(price); // 999