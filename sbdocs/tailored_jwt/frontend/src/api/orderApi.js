import { get, post } from './client';

export const orderApi = {
  getAll: () => get('/api/orders'),
  getById: (id) => get(`/api/orders/${id}`),
  create: (customerName, items) => post('/api/orders', { customerName, items }),
  byCustomer: (name) => get(`/api/orders/customer/${encodeURIComponent(name)}`),
  customerTotal: (name) => get(`/api/orders/customer/${encodeURIComponent(name)}/total`),
  customerCount: (name) => get(`/api/orders/customer/${encodeURIComponent(name)}/count`),
};
