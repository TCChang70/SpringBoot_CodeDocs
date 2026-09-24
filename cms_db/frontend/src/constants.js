// 狀態／角色顯示文字（集中管理，避免散落各頁）

export const ARTICLE_STATUS_LABELS = {
  draft: '草稿',
  pending_review: '待審核',
  published: '已發佈',
  archived: '已封存',
};

export const COMMENT_STATUS_LABELS = {
  pending: '待審核',
  approved: '已核准',
  spam: '垃圾',
};

export const ROLE_LABELS = {
  admin: '管理員',
  editor: '編輯',
  author: '作者',
  subscriber: '訂閱者',
};

export const ROLE_OPTIONS = Object.keys(ROLE_LABELS);