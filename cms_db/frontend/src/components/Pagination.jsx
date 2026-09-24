// 分頁列：page 從 0 開始（對應後端 PageResult）
export default function Pagination({ page, totalPages, totalElements, onChange }) {
  if (!totalPages) return null;
  return (
    <div className="pagination">
      {totalElements != null && <span className="muted">共 {totalElements} 筆</span>}
      <button className="btn btn-sm" disabled={page <= 0} onClick={() => onChange(page - 1)}>上一頁</button>
      <span>第 {page + 1} / {totalPages} 頁</span>
      <button className="btn btn-sm" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>下一頁</button>
    </div>
  );
}