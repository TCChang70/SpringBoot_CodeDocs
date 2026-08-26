import { useEffect, useState } from 'react';
import { getLowStockProducts, getThreshold, saveThreshold } from '../services/productService';
import { Product } from '../models/product';
import { StockThreshold } from '../models/stock-summary';

function LowStockAlert() {
  const [lowStockProducts, setLowStockProducts] = useState<Product[]>([]);
  const [thresholdMap, setThresholdMap]           = useState<Record<string, StockThreshold>>({});

  const [editingCode, setEditingCode]     = useState('');
  const [editMinQty, setEditMinQty]       = useState(200);
  const [editReorderQty, setEditReorderQty] = useState(500);

  useEffect(() => { loadLowStock(); }, []);

  function loadLowStock() {
    getLowStockProducts().then((products) => {
      setLowStockProducts(products);
      products.forEach((p) => {
        getThreshold(p.productCode)
          .then((t) => setThresholdMap((prev) => ({ ...prev, [p.productCode]: t })))
          .catch(() => {});
      });
    });
  }

  function startEdit(product: Product) {
    const existing = thresholdMap[product.productCode];
    setEditingCode(product.productCode);
    setEditMinQty(existing?.minQuantity ?? 200);
    setEditReorderQty(existing?.reorderQuantity ?? 500);
  }

  function handleSave(productCode: string) {
    saveThreshold(productCode, editMinQty, editReorderQty).then((t) => {
      setThresholdMap((prev) => ({ ...prev, [productCode]: t }));
      setEditingCode('');
    });
  }

  return (
    <div className="container py-4">
      <h2 className="mb-3">低庫存警示管理</h2>

      <div className="alert alert-info">
        <strong>說明：</strong>
        當產品庫存低於該產品設定的安全水位時，將顯示於此清單。
        預設安全水位為 200 件，建議補貨量為 500 件。
      </div>

      <table className="table table-bordered">
        <thead className="table-light">
          <tr>
            <th>產品代碼</th>
            <th>產品名稱</th>
            <th>產品線</th>
            <th>供應商</th>
            <th className="text-end">目前庫存</th>
            <th className="text-end">安全水位</th>
            <th className="text-end">建議補貨量</th>
            <th className="text-center">危急程度</th>
            <th className="text-center">操作</th>
          </tr>
        </thead>
        <tbody>
          {lowStockProducts.length === 0 ? (
            <tr>
              <td colSpan={9} className="text-center text-success py-3">
                目前無低庫存警示
              </td>
            </tr>
          ) : (
            lowStockProducts.map((p) => {
              const threshold = thresholdMap[p.productCode];
              const isEditing = editingCode === p.productCode;

              return (
                <tr key={p.productCode}>
                  <td>{p.productCode}</td>
                  <td>{p.productName}</td>
                  <td>{p.productLine}</td>
                  <td>{p.productVendor}</td>
                  <td className="text-end">
                    <span className={p.quantityInStock < 100 ? 'text-danger fw-bold' : 'text-warning fw-bold'}>
                      {p.quantityInStock.toLocaleString()}
                    </span>
                  </td>
                  <td className="text-end">
                    {isEditing ? (
                      <input type="number" className="form-control form-control-sm d-inline-block"
                             style={{ width: 80 }} value={editMinQty} min={1}
                             onChange={(e) => setEditMinQty(Number(e.target.value))} />
                    ) : (
                      threshold?.minQuantity?.toLocaleString() ?? 200
                    )}
                  </td>
                  <td className="text-end">
                    {isEditing ? (
                      <input type="number" className="form-control form-control-sm d-inline-block"
                             style={{ width: 80 }} value={editReorderQty} min={1}
                             onChange={(e) => setEditReorderQty(Number(e.target.value))} />
                    ) : (
                      threshold?.reorderQuantity?.toLocaleString() ?? 500
                    )}
                  </td>
                  <td className="text-center">
                    <span className={`badge bg-${p.quantityInStock < 100 ? 'danger' : 'warning'}`}>
                      {p.quantityInStock < 100 ? '緊急' : '警示'}
                    </span>
                  </td>
                  <td className="text-center">
                    {isEditing ? (
                      <>
                        <button className="btn btn-success btn-sm" onClick={() => handleSave(p.productCode)}>
                          儲存
                        </button>
                        <button className="btn btn-secondary btn-sm ms-1" onClick={() => setEditingCode('')}>
                          取消
                        </button>
                      </>
                    ) : (
                      <button className="btn btn-outline-primary btn-sm" onClick={() => startEdit(p)}>
                        設定水位
                      </button>
                    )}
                  </td>
                </tr>
              );
            })
          )}
        </tbody>
      </table>
    </div>
  );
}

export default LowStockAlert;