import { get, post } from './client';

export const categoryApi = {
  getAll: () => get('/api/categories'),
  getAllWithProducts: () => get('/api/categories/with-products'),
  getById: (id) => get(`/api/categories/${id}`),
  create: (name) => post('/api/categories', { name }),
};
