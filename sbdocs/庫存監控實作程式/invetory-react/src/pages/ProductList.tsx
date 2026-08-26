import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getProducts, getProductLines, getVendors, getProductById } from '../services/productService';
import { Product } from '../models/product';

// ── 產品列表（無 id 參數時顯示全部列表） ────────────
function ProductList() {
  const [products, setProducts]       = useState<Product[]>([]);
  const [productLines, setProductLines] = useState<string[]>([]);
  const [vendors, setVendors]         = useState<string[]>([]);

  const [selectedLine, setSelectedLine]   = useState('');
  const [selectedVendor, setSelectedVendor] = useState('');
  const [keyword, setKeyword]             = useState('');

  useEffect(() => {
    getProductLines().then(setProductLines);
    getVendors().then(setVendors);
    loadProducts();
  }, []);

  function loadProducts() {
    getProducts({
      productLine: selectedLine   || undefined,
      vendor:      selectedVendor || undefined,
      keyword:     keyword        || undefined
    }).then(setProducts);
  }

  function calcMargin(buyPrice: number, msrp: number): string {
    if (msrp === 0) return '0%';
    return (((msrp - buyPrice) / msrp) * 100).toFixed(1) + '%';
  }

  return (
    <div className="container py-4">
      <h2 className="mb-3">產品管理</h2>

      {/* 篩選列 */}
      <div className="row g-2 mb-3">
        <div className="col-md-3">
          <input type="text" className="form-control" placeholder="搜尋產品名稱或描述..."
                 value={keyword} onChange={(e) => setKeyword(e.target.value)} />
        </div>
        <div className="col-md-2">
          <select className="form-select" value={selectedLine}
                  onChange={(e) => setSelectedLine(e.target.value)}>
            <option value="">-- 所有產品線 --</option>
            {productLines.map((line) => <option key={line} value={line}>{line}</option>)}
          </select>
        </div>
        <div className="col-md-2">
          <select className="form-select" value={selectedVendor}
                  onChange={(e) => setSelectedVendor(e.target.value)}>
            <option value="">-- 所有供應商 --</option>
            {vendors.map((v) => <option key={v} value={v}>{v}</option>)}
          </select>
        </div>
        <div className="col-md-1">
          <button className="btn btn-primary w-100" onClick={loadProducts}>搜尋</button>
        </div>
        <div className="col-md-1">
          <button className="btn btn-secondary w-100" onClick={() => {
            setSelectedLine(''); setSelectedVendor(''); setKeyword('');
          }}>重置</button>
        </div>
      </div>

      <p className="text-muted">共 {products.length} 筆產品</p>

      {/* 產品列表 */}
      <table className="table table-bordered table-hover">
        <thead className="table-light">
          <tr>
            <th>產品代碼</th>
            <th>產品名稱</th>
            <th>產品線</th>
            <th>比例</th>
            <th>供應商</th>
            <th className="text-end">進價 (USD)</th>
            <th className="text-end">建議售價 (USD)</th>
            <th className="text-end">毛利率</th>
            <th className="text-end">庫存數量</th>
            <th className="text-center">操作</th>
          </tr>
        </thead>
        <tbody>
          {products.length === 0 ? (
            <tr><td colSpan={10} className="text-center text-muted py-3">查無資料</td></tr>
          ) : (
            products.map((p) => (
              <tr key={p.productCode}>
                <td>{p.productCode}</td>
                <td>{p.productName}</td>
                <td>{p.productLine}</td>
                <td>{p.productScale}</td>
                <td>{p.productVendor}</td>
                <td className="text-end">{p.buyPrice.toFixed(2)}</td>
                <td className="text-end">{p.msrp.toFixed(2)}</td>
                <td className="text-end">{calcMargin(p.buyPrice, p.msrp)}</td>
                <td className={`text-end ${p.quantityInStock < 500 ? 'text-danger fw-bold' : ''}`}>
                  {p.quantityInStock.toLocaleString()}
                </td>
                <td className="text-center">
                  <Link to={`/products/${p.productCode}`} className="btn btn-outline-primary btn-sm">
                    查看
                  </Link>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

// ── 產品明細頁（含路由參數 id） ────────────────────
function ProductDetail() {
  const { id } = useParams<{ id: string }>();
  const [product, setProduct] = useState<Product | null>(null);

  useEffect(() => {
    if (id) getProductById(id).then(setProduct);
  }, [id]);

  if (!product) return <div className="container py-4">載入中...</div>;

  const margin = product.msrp === 0 ? '0%' : (((product.msrp - product.buyPrice) / product.msrp) * 100).toFixed(1) + '%';

  return (
    <div className="container py-4">
      <Link to="/products" className="btn btn-outline-secondary btn-sm mb-3">
        &larr; 返回列表
      </Link>
      <div className="card shadow-sm">
        <div className="card-header bg-primary text-white">
          <h5 className="mb-0">產品明細 — {product.productCode}</h5>
        </div>
        <div className="card-body">
          <table className="table table-borderless mb-0">
            <tbody>
              <tr><th>產品名稱</th><td>{product.productName}</td></tr>
              <tr><th>產品線</th><td>{product.productLine}</td></tr>
              <tr><th>比例</th><td>{product.productScale}</td></tr>
              <tr><th>供應商</th><td>{product.productVendor}</td></tr>
              <tr><th>進價</th><td>{product.buyPrice.toFixed(2)} USD</td></tr>
              <tr><th>建議售價</th><td>{product.msrp.toFixed(2)} USD</td></tr>
              <tr><th>毛利率</th><td>{margin}</td></tr>
              <tr>
                <th>庫存數量</th>
                <td className={product.quantityInStock < 500 ? 'text-danger fw-bold' : ''}>
                  {product.quantityInStock.toLocaleString()}
                </td>
              </tr>
              <tr><th>產品描述</th><td>{product.productDescription}</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

export { ProductList, ProductDetail };