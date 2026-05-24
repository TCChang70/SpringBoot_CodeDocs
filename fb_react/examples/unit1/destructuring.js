const user = { name: "Alice", age: 25, city: "Taipei" };

// 傳統寫法
//const name = user.name;
//const age  = user.age;

// 解構寫法（一行搞定）
const { name, age } = user;
console.log(`User name: ${name}, age: ${age}`); // "User name: Alice, age: 25"
// 重新命名
const { name: userName } = user;
console.log(userName); // "Alice"

// 設定預設值
const { city, country = "Taiwan" } = user;
console.log(country); // "Taiwan"（user 裡沒有 country）