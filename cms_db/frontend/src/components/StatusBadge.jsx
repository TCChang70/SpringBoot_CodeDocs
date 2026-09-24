// 狀態徽章：依型別（article / comment）與狀態上色
import { ARTICLE_STATUS_LABELS, COMMENT_STATUS_LABELS } from '../constants.js';

export default function StatusBadge({ status, type = 'article' }) {
  const labels = type === 'comment' ? COMMENT_STATUS_LABELS : ARTICLE_STATUS_LABELS;
  return <span className={`badge badge-${status}`}>{labels[status] || status}</span>;
}