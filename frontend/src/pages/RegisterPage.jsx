import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await register(email, password);
      navigate("/");
    } catch (err) {
      // Lấy message hoặc lỗi validation đầu tiên từ backend
      const data = err.response?.data;
      const fieldError = data?.fieldErrors && Object.values(data.fieldErrors)[0];
      setError(fieldError || data?.message || "Đăng ký thất bại");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-wrap">
      <form className="card auth-card" onSubmit={handleSubmit}>
        <h1>Đăng ký</h1>
        <p className="muted">Tạo tài khoản mới</p>

        {error && <div className="alert error">{error}</div>}

        <label>Email</label>
        <input type="email" value={email} required
               onChange={(e) => setEmail(e.target.value)} placeholder="ban@example.com" />

        <label>Mật khẩu (tối thiểu 6 ký tự)</label>
        <input type="password" value={password} required minLength={6}
               onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />

        <button className="btn primary" type="submit" disabled={loading}>
          {loading ? "Đang tạo..." : "Đăng ký"}
        </button>

        <p className="muted center">
          Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
        </p>
      </form>
    </div>
  );
}
