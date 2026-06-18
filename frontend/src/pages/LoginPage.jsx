import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();           // chặn reload trang mặc định của form
    setError("");
    setLoading(true);
    try {
      await login(email, password);
      navigate("/");              // đăng nhập xong -> về dashboard
    } catch (err) {
      setError(err.response?.data?.message || "Đăng nhập thất bại");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-wrap">
      <form className="card auth-card" onSubmit={handleSubmit}>
        <h1>Đăng nhập</h1>
        <p className="muted">Personal Finance Manager</p>

        {error && <div className="alert error">{error}</div>}

        <label>Email</label>
        <input type="email" value={email} required
               onChange={(e) => setEmail(e.target.value)} placeholder="demo@finance.local" />

        <label>Mật khẩu</label>
        <input type="password" value={password} required
               onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />

        <button className="btn primary" type="submit" disabled={loading}>
          {loading ? "Đang đăng nhập..." : "Đăng nhập"}
        </button>

        <p className="muted center">
          Chưa có tài khoản? <Link to="/register">Đăng ký</Link>
        </p>
      </form>
    </div>
  );
}
