// 錯誤邊界：捕捉渲染期錯誤，避免整頁白畫面
// 用法：<ErrorBoundary><App /></ErrorBoundary>
import { Component } from 'react';

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { error: null };
  }

  static getDerivedStateFromError(error) {
    return { error };
  }

  render() {
    if (this.state.error) {
      return (
        <div className="container" style={{ paddingTop: 30 }}>
          <div className="card">
            <h2>頁面發生錯誤</h2>
            <p className="alert alert-error">{String(this.state.error?.message || this.state.error)}</p>
            <button className="btn btn-primary" onClick={() => window.location.reload()}>重新整理</button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}