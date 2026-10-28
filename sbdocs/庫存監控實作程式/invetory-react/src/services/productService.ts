import axios from 'axios';
import { Product, ApiResponse } from '../models/product';
import { StockSummary, StockThreshold } from '../models/stock-summary';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { mockProducts, mockThresholds } from './mockData';

const API_URL = 'http://localhost:8080/api';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const USE_MOCK_API = true; // Set to false when backend is available

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
  if (USE_MOCK_API) {
    let result = [...mockProducts];
    if (filters?.productLine) {
      result = result.filter(p => p.productLine === filters.productLine);
    }
    if (filters?.vendor) {
      result = result.filter(p => p.productVendor === filters.vendor);
    }
    if (filters?.keyword) {
      const kw = filters.keyword.toLowerCase();
      result = result.filter(p => 
        p.productName.toLowerCase().includes(kw) || 
        p.productDescription.toLowerCase().includes(kw)
      );
    }
    return result;
  }

  const params: Record<string, string> = {};
  if (filters?.productLine) params.productLine = filters.productLine;
  if (filters?.vendor)      params.vendor = filters.vendor;
  if (filters?.keyword)     params.keyword = filters.keyword;
  return unwrap<Product[]>(api.get('/products', { params }));
}

export async function getProductById(productCode: string): Promise<Product> {
  if (USE_MOCK_API) {
    const product = mockProducts.find(p => p.productCode === productCode);
    if (!product) throw new Error('Product not found');
    return product;
  }
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
  if (USE_MOCK_API) {
    return mockProducts.filter(p => p.quantityInStock < 100);
  }
  return unwrap<Product[]>(api.get('/products/low-stock'));
}

export async function getDashboardSummary(): Promise<StockSummary[]> {
  if (USE_MOCK_API) {
    const summary: { [key: string]: StockSummary } = {};
    mockProducts.forEach(p => {
      if (!summary[p.productLine]) {
        summary[p.productLine] = {
          productLine: p.productLine,
          totalStock: 0,
          productCount: 0,
          minStock: p.quantityInStock,
        };
      }
      summary[p.productLine].totalStock += p.quantityInStock;
      summary[p.productLine].productCount += 1;
      summary[p.productLine].minStock = Math.min(summary[p.productLine].minStock, p.quantityInStock);
    });
    return Object.values(summary);
  }
  return unwrap<StockSummary[]>(api.get('/products/dashboard/summary'));
}

// ── 篩選選項 ────────────────────────────────────────

export async function getProductLines(): Promise<string[]> {
  if (USE_MOCK_API) {
    return [...new Set(mockProducts.map(p => p.productLine))];
  }
  return unwrap<string[]>(api.get('/products/filter/product-lines'));
}

export async function getVendors(): Promise<string[]> {
  if (USE_MOCK_API) {
    return [...new Set(mockProducts.map(p => p.productVendor))];
  }
  return unwrap<string[]>(api.get('/products/filter/vendors'));
}

// ── 安全水位 ────────────────────────────────────────

export async function getThreshold(productCode: string): Promise<StockThreshold> {
  if (USE_MOCK_API) {
    return mockThresholds[productCode as keyof typeof mockThresholds] || 
      { id: 0, productCode, minQuantity: 200, reorderQuantity: 500, updatedAt: new Date().toISOString() };
  }
  return unwrap<StockThreshold>(api.get(`/thresholds/${productCode}`));
}

export async function saveThreshold(
  productCode: string,
  minQuantity: number,
  reorderQuantity: number
): Promise<StockThreshold> {
  if (USE_MOCK_API) {
    const threshold = mockThresholds[productCode as keyof typeof mockThresholds];
    if (threshold) {
      threshold.minQuantity = minQuantity;
      threshold.reorderQuantity = reorderQuantity;
      threshold.updatedAt = new Date().toISOString();
    }
    return threshold || { id: 0, productCode, minQuantity, reorderQuantity, updatedAt: new Date().toISOString() };
  }
  return unwrap<StockThreshold>(
    api.put(`/thresholds/${productCode}`, { minQuantity, reorderQuantity })
  );
}