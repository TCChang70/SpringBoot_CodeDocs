export interface Product {
  productCode: string;
  productName: string;
  productLine: string;
  productScale: string;
  productVendor: string;
  productDescription: string;
  quantityInStock: number;
  buyPrice: number;
  msrp: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}