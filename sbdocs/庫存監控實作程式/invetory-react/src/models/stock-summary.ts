export interface StockSummary {
  productLine: string;
  totalStock: number;
  productCount: number;
  minStock: number;
}

export interface StockThreshold {
  id: number;
  productCode: string;
  minQuantity: number;
  reorderQuantity: number;
  updatedAt: string;
}