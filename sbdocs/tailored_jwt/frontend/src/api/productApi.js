import { get, post, put, del } from './client';

export const productApi = {
  getAll: () => get('/api/products'),
  getById: (id) => get(`/api/products/${id}`),
  create: (data) => post('/api/products', data),
  update: (id, data) => put(`/api/products/${id}`, data),
  remove: (id) => del(`/api/products/${id}`),

  placeOrder: (id, quantity) => get(`/api/products/${id}/place-order?quantity=${quantity}`),
  updatePrice: (id, price) => get(`/api/products/${id}/update-price?price=${price}`),

  searchByName: (keyword) => get(`/api/products/search?keyword=${encodeURIComponent(keyword)}`),
  searchNative: (keyword) =>
    get(`/api/products/native-search?keyword=${encodeURIComponent(keyword)}`),
  byBrand: (brand) => get(`/api/products/brand/${encodeURIComponent(brand)}`),
  byBrandExpensive: (brand, minPrice) =>
    get(`/api/products/brand/${encodeURIComponent(brand)}/expensive?minPrice=${minPrice}`),
  cheap: (maxPrice) => get(`/api/products/cheap?maxPrice=${maxPrice}`),
  brandCount: (brand) => get(`/api/products/brand/${encodeURIComponent(brand)}/count`),
  existsByName: (name) => get(`/api/products/exists?name=${encodeURIComponent(name)}`),
  availableByCategory: (cat) =>
    get(`/api/products/category/${encodeURIComponent(cat)}/available`),
  avgPriceByCategory: (cat) =>
    get(`/api/products/category/${encodeURIComponent(cat)}/avg-price`),
  clearStockByCategory: (cat) =>
    post(`/api/products/category/${encodeURIComponent(cat)}/clear-stock`),
  paged: (page, size, sortBy) => get(`/api/products/page?page=${page}&size=${size}&sortBy=${sortBy}`),
};
