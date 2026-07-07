import { Component } from "react";

/**
 * Lưới an toàn cho toàn app: nếu BẤT KỲ component con nào ném lỗi khi render,
 * React sẽ gọi vào đây thay vì để màn hình trắng xóa. Hiện thông báo thân thiện
 * + nút tải lại, giữ được thanh điều hướng của trình duyệt.
 *
 * Error boundary BẮT BUỘC là class component — React chưa có bản hook tương đương.
 */
export default class ErrorBoundary extends Component {
  state = { hasError: false };

  // Khi con ném lỗi -> đổi state để lần render kế hiện UI dự phòng.
  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error, info) {
    // Ghi log để còn lần theo (production có thể gắn Sentry ở đây).
    console.error("Lỗi không mong muốn trong app:", error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="auth-wrap">
          <div className="card auth-card" style={{ textAlign: "center" }}>
            <h1>Có gì đó trục trặc 😥</h1>
            <p className="muted">
              Giao diện gặp lỗi ngoài dự kiến. Bạn thử tải lại trang nhé —
              dữ liệu của bạn vẫn an toàn.
            </p>
            <button className="btn primary" onClick={() => window.location.reload()}>
              Tải lại trang
            </button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}
