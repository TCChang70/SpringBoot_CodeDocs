const fruits = ["蘋果", "香蕉", "芒果"];

export function FruitList() {
  return (
    <ul>
      {fruits.map((fruit, index) => (
        // key 必須是唯一且穩定的值（不要用 index，盡量用 id）
        <li key={index}>{fruit}</li>
      ))}
    </ul>
  );
}

// 更好的做法：使用資料中的唯一 id
const products = [
  { id: 1, name: "iPhone", price: 999 },
  { id: 2, name: "MacBook", price: 1999 },
];

export function ProductList() {
  return (
    <ul>
      {products.map(product => (
        <li key={product.id}>
          {product.name} — ${product.price}
        </li>
      ))}
    </ul>
  );
}