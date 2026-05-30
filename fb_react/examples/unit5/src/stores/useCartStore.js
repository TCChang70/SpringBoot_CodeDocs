// stores/useCartStore.js
import { create } from 'zustand';

const useCartStore = create((set, get) => ({
  // ===== State =====
  items: [],
  isOpen: false,

  // ===== 衍生計算（Getters）=====
  get itemCount() {
    return get().items.reduce((sum, i) => sum + i.quantity, 0);
  },
  get total() {
    return get().items.reduce((sum, i) => sum + i.price * i.quantity, 0);
  },

  // ===== Actions =====
  addItem: (product) =>
    set((state) => {
      const exists = state.items.find(i => i.id === product.id);
      if (exists) {
        return {
          items: state.items.map(i =>
            i.id === product.id ? { ...i, quantity: i.quantity + 1 } : i
          ),
        };
      }
      return { items: [...state.items, { ...product, quantity: 1 }] };
    }),

  removeItem: (id) =>
    set((state) => ({
      items: state.items.filter(i => i.id !== id),
    })),

  updateQuantity: (id, quantity) =>
    set((state) => ({
      items: quantity <= 0
        ? state.items.filter(i => i.id !== id)
        : state.items.map(i => i.id === id ? { ...i, quantity } : i),
    })),

  clearCart: () => set({ items: [] }),

  toggleCart: () => set((state) => ({ isOpen: !state.isOpen })),
}));

export default useCartStore;