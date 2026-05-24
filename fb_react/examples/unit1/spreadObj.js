const user = { name: "Alice", age: 25 };

// 複製物件
const copy = { ...user };

// 新增屬性
const updatedUser = { ...user, city: "Taipei" };
console.log(updatedUser); // { name: "Alice", age: 25, city: "Taipei" }

// 覆蓋屬性（後面的覆蓋前面的）
const renamedUser = { ...user, name: "Bob" };
console.log(renamedUser); // { name: "Bob", age: 25 }