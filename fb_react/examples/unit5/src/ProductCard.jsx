// 元件中使用 — 不需要 Provider！直接 import 並使用
import useCartStore from './stores/useCartStore';

export default function ProductCard({ product }) {
  // 只訂閱需要的部分（效能最佳化：只有 addItem 改變才重渲染）
  const addItem = useCartStore((state) => state.addItem);

  return (
    <div>
      <h3>{product.name}</h3>
      <button onClick={() => addItem(product)}>加入購物車</button>
    </div>
  );
}

export function CartIcon() {
  // 用獨立 selector 避免每次回傳新物件導致無限重渲染
  const itemCount = useCartStore((state) =>
    state.items.reduce((sum, i) => sum + i.quantity, 0)
  );
  const toggleCart = useCartStore((state) => state.toggleCart);

  return (
    <button onClick={toggleCart}>
      🛒 {itemCount > 0 && <span className="cart-badge">{itemCount}</span>}
    </button>
  );
}

export function CartSidebar() {
  const items = useCartStore((state) => state.items);
  const isOpen = useCartStore((state) => state.isOpen);
  const removeItem = useCartStore((state) => state.removeItem);
  const updateQuantity = useCartStore((state) => state.updateQuantity);
  const clearCart = useCartStore((state) => state.clearCart);
  // total 由元件自行計算，避免依賴 store getter
  const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0);

  return (
    <aside className={isOpen ? 'cart-open' : ''}>
      {items.map(item => (
        <div key={item.id}>
          <span>{item.name} x {item.quantity}</span>
          <button onClick={() => updateQuantity(item.id, item.quantity - 1)}>-</button>
          <button onClick={() => updateQuantity(item.id, item.quantity + 1)}>+</button>
          <button onClick={() => removeItem(item.id)}>移除</button>
        </div>
      ))}
      <p>總計：${total.toFixed(2)}</p>
      <button onClick={clearCart}>清空購物車</button>
    </aside>
  );
}