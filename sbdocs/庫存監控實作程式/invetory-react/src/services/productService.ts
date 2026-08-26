import axios from 'axios';
import { Product, ApiResponse } from '../models/product';
import { StockSummary, StockThreshold } from '../models/stock-summary';

const API_URL = 'http://localhost:8080/api';

// ── 通用 axios 實例 ──────────────────────────────────
const api = axios.create({ baseURL: API_URL });

// 从 ApiResponse<T> 中解出 data
async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const res = await promise;
  return res.data.data;
}

// ── 產品 CRUD ────────────────────────────────────────

export async function getProducts(filters?: {
  productLine?: string;
  vendor?: string;
  keyword?: string;
}): Promise<Product[]> {
  const params: Record<string, string> = {};
  if (filters?.productLine) params.productLine = filters.productLine;
  if (filters?.vendor)      params.vendor = filters.vendor;
  if (filters?.keyword)     params.keyword = filters.keyword;
  return unwrap<Product[]>(api.get('/products', { params }));
}

export async function getProductById(productCode: string): Promise<Product> {
  return unwrap<Product>(api.get(`/products/${productCode}`));
}

export async function createProduct(product: Product): Promise<Product> {
  return unwrap<Product>(api.post('/products', product));
}

export async function updateProduct(productCode: string, product: Product): Promise<Product> {
  return unwrap<Product>(api.put(`/products/${productCode}`, product));
}

export async function deleteProduct(productCode: string): Promise<void> {
  await api.delete(`/products/${productCode}`);
}

// ── 庫存監控 ────────────────────────────────────────

export async function getLowStockProducts(): Promise<Product[]> {
  return unwrap<Product[]>(api.get('/products/low-stock'));
}

export async function getDashboardSummary(): Promise<StockSummary[]> {
  return unwrap<StockSummary[]>(api.get('/products/dashboard/summary'));
}

// ── 篩選選項 ────────────────────────────────────────

export async function getProductLines(): Promise<string[]> {
  return unwrap<string[]>(api.get('/products/filter/product-lines'));
}

export async function getVendors(): Promise<string[]> {
  return unwrap<string[]>(api.get('/products/filter/vendors'));
}

// ── 安全水位 ────────────────────────────────────────

export async function getThreshold(productCode: string): Promise<StockThreshold> {
  return unwrap<StockThreshold>(api.get(`/thresholds/${productCode}`));
}

export async function saveThreshold(
  productCode: string,
  minQuantity: number,
  reorderQuantity: number
): Promise<StockThreshold> {
  return unwrap<StockThreshold>(
    api.put(`/thresholds/${productCode}`, { minQuantity, reorderQuantity })
  );
}