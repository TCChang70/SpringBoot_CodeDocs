// 訊息框：error / success 變體
export default function Alert({ variant = 'info', children }) {
  if (!children) return null;
  return <p className={`alert alert-${variant}`}>{children}</p>;
}