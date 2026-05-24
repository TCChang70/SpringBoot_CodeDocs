import { useReducer } from 'react';

// 初始狀態
const initialState = {
  items: [],
  total: 0,
};

// Reducer 函式：定義所有狀態轉換邏輯（純函式）
function cartReducer(state, action) {
  switch (action.type) {
    case 'ADD_ITEM': {
      const existingItem = state.items.find(item => item.id === action.payload.id);
      if (existingItem) {
        // 已有此商品，增加數量
        const updatedItems = state.items.map(item =>
          item.id === action.payload.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
        return {
          items: updatedItems,
          total: state.total + action.payload.price,
        };
      }
      // 新商品
      return {
        items: [...state.items, { ...action.payload, quantity: 1 }],
        total: state.total + action.payload.price,
      };
    }

    case 'REMOVE_ITEM': {
      const item = state.items.find(i => i.id === action.payload);
      return {
        items: state.items.filter(i => i.id !== action.payload),
        total: state.total - (item.price * item.quantity),
      };
    }

    case 'CLEAR_CART':
      return initialState;

    default:
      return state; // 未知 action，回傳原 state
  }
}

function ShoppingCart() {
  const [cart, dispatch] = useReducer(cartReducer, initialState);

  const addItem = (product) => {
    dispatch({ type: 'ADD_ITEM', payload: product });
  };

  const removeItem = (id) => {
    dispatch({ type: 'REMOVE_ITEM', payload: id });
  };

  return (
    <div>
      <h2>購物車（{cart.items.length} 項）</h2>
      <ul>
        {cart.items.map(item => (
          <li key={item.id}>
            {item.name} x {item.quantity} — ${item.price * item.quantity}
            <button onClick={() => removeItem(item.id)}>移除</button>
          </li>
        ))}
      </ul>
      <p>總計：${cart.total}</p>
      <button onClick={() => dispatch({ type: 'CLEAR_CART' })}>清空購物車</button>

      {/* 商品列表 */}
      <hr />
      <h3>商品</h3>
      {[
        { id: 1, name: "iPhone", price: 999 },
        { id: 2, name: "AirPods", price: 249 },
      ].map(product => (
        <div key={product.id}>
          {product.name} ${product.price}
          <button onClick={() => addItem(product)}>加入購物車</button>
        </div>
      ))}
    </div>
  );
}

export default ShoppingCart;