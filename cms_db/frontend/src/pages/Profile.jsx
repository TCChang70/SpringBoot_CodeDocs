// 個人資料（後端 §6.1~6.3）
import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext.jsx';
import { me, updateProfile, changePassword } from '../api/users.js';
import Alert from '../components/Alert.jsx';
import { ROLE_LABELS } from '../constants.js';

export default function Profile() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [profileForm, setProfileForm] = useState({ displayName: '', avatarUrl: '' });
  const [pwdForm, setPwdForm] = useState({ oldPassword: '', newPassword: '', confirm: '' });
  const [msg, setMsg] = useState(null);
  const [err, setErr] = useState(null);

  useEffect(() => {
    me().then((u) => {
      setProfile(u);
      setProfileForm({ displayName: u.displayName || '', avatarUrl: u.avatarUrl || '' });
    }).catch((e) => setErr(e.message));
  }, []);

  const saveProfile = async (e) => {
    e.preventDefault();
    setErr(null);
    setMsg(null);
    try {
      const u = await updateProfile(profileForm);
      setProfile(u);
      setMsg('個人資料已更新');
    } catch (err) {
      setErr(err.message);
    }
  };

  const submitPwd = async (e) => {
    e.preventDefault();
    setErr(null);
    setMsg(null);
    if (pwdForm.newPassword !== pwdForm.confirm) {
      setErr('兩次輸入的新密碼不一致');
      return;
    }
    try {
      await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword });
      setPwdForm({ oldPassword: '', newPassword: '', confirm: '' });
      setMsg('密碼已更新');
    } catch (err) {
      setErr(err.message);
    }
  };

  return (
    <div>
      <div className="page-title"><h1>個人資料</h1></div>
      {err && <Alert variant="error">{err}</Alert>}
      {msg && <Alert variant="success">{msg}</Alert>}

      <div className="card">
        <h3>基本資料</h3>
        {profile ? (
          <>
            <p className="muted">
              帳號：{profile.username} ｜ 角色：{ROLE_LABELS[profile.role]} ｜ 建立於 {profile.createdAt}
            </p>
            <form onSubmit={saveProfile}>
              <div className="field">
                <label>顯示名稱（顯示在文章/留言上的名字）</label>
                <input className="input" value={profileForm.displayName} onChange={(e) => setProfileForm({ ...profileForm, displayName: e.target.value })} />
              </div>
              <div className="field">
                <label>頭像網址</label>
                <input className="input" value={profileForm.avatarUrl} onChange={(e) => setProfileForm({ ...profileForm, avatarUrl: e.target.value })} />
              </div>
              <button className="btn btn-primary">儲存</button>
            </form>
          </>
        ) : (
          <p className="muted">載入中…</p>
        )}
      </div>

      <div className="card">
        <h3>修改密碼</h3>
        <form onSubmit={submitPwd}>
          <div className="field">
            <label>目前密碼</label>
            <input type="password" className="input" value={pwdForm.oldPassword} onChange={(e) => setPwdForm({ ...pwdForm, oldPassword: e.target.value })} />
          </div>
          <div className="form-row">
            <div className="field">
              <label>新密碼（8~72 字元）</label>
              <input type="password" className="input" value={pwdForm.newPassword} onChange={(e) => setPwdForm({ ...pwdForm, newPassword: e.target.value })} />
            </div>
            <div className="field">
              <label>確認新密碼</label>
              <input type="password" className="input" value={pwdForm.confirm} onChange={(e) => setPwdForm({ ...pwdForm, confirm: e.target.value })} />
            </div>
          </div>
          <button className="btn btn-primary">更新密碼</button>
        </form>
      </div>
    </div>
  );
}