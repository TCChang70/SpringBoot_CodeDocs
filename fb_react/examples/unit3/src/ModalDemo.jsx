import { useState, useCallback } from 'react';
import Modal from './components/Modal';

function ModalDemo() {
  const [deleteModal, setDeleteModal] = useState(false);
  const [infoModal, setInfoModal] = useState(false);
  const [formModal, setFormModal] = useState(false);
  const [inputValue, setInputValue] = useState('');
  const [confirmed, setConfirmed] = useState(null);

  const handleConfirmDelete = useCallback(() => {
    setConfirmed('已刪除該筆資料');
    setDeleteModal(false);
  }, []);

  const handleFormSubmit = useCallback(() => {
    setConfirmed(`已送出：${inputValue}`);
    setInputValue('');
    setFormModal(false);
  }, [inputValue]);

  return (
    <div style={{ padding: '40px', fontFamily: 'sans-serif' }}>
      <h1>Modal 元件示範</h1>

      {confirmed && (
        <p style={{ color: 'green', fontWeight: 'bold' }}>✔ {confirmed}</p>
      )}

      <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
        {/* 確認刪除 */}
        <button onClick={() => setDeleteModal(true)}>🗑 刪除資料</button>

        {/* 純資訊 */}
        <button onClick={() => setInfoModal(true)}>ℹ 查看說明</button>

        {/* 含表單 */}
        <button onClick={() => setFormModal(true)}>✏ 填寫備註</button>
      </div>

      {/* --- 確認刪除 Modal --- */}
      <Modal
        isOpen={deleteModal}
        onClose={() => setDeleteModal(false)}
        title="確認刪除"
        footer={
          <>
            <button onClick={() => setDeleteModal(false)}>取消</button>
            <button
              onClick={handleConfirmDelete}
              style={{ marginLeft: '8px', background: '#e53e3e', color: '#fff', border: 'none', padding: '6px 14px', borderRadius: '4px', cursor: 'pointer' }}
            >
              確認刪除
            </button>
          </>
        }
      >
        <p>確定要刪除這筆資料嗎？此操作無法復原。</p>
      </Modal>

      {/* --- 資訊說明 Modal --- */}
      <Modal
        isOpen={infoModal}
        onClose={() => setInfoModal(false)}
        title="使用說明"
        footer={
          <button onClick={() => setInfoModal(false)}>關閉</button>
        }
      >
        <ul>
          <li>點擊背景或按 ESC 可關閉視窗</li>
          <li>Modal 透過 createPortal 掛載至 body</li>
          <li>開啟時背景無法滾動</li>
        </ul>
      </Modal>

      {/* --- 含表單 Modal --- */}
      <Modal
        isOpen={formModal}
        onClose={() => setFormModal(false)}
        title="填寫備註"
        footer={
          <>
            <button onClick={() => setFormModal(false)}>取消</button>
            <button
              onClick={handleFormSubmit}
              style={{ marginLeft: '8px', background: '#3182ce', color: '#fff', border: 'none', padding: '6px 14px', borderRadius: '4px', cursor: 'pointer' }}
            >
              送出
            </button>
          </>
        }
      >
        <label htmlFor="note">備註內容：</label>
        <br />
        <textarea
          id="note"
          rows={4}
          style={{ width: '100%', marginTop: '8px', boxSizing: 'border-box' }}
          value={inputValue}
          onChange={e => setInputValue(e.target.value)}
          placeholder="請輸入備註..."
        />
      </Modal>
    </div>
  );
}

export default ModalDemo;
