import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { useConcept } from "../theme/ConceptContext";
import AuthDecor from "../components/AuthDecor";

export default function LoginPage() {
  const { concept } = useConcept();
  const garden = concept === "garden";
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
        <h1>{garden ? "🌱 Vườn Xanh" : "💰 Finance"}</h1>
        <p className="muted">
          {garden ? "Chào mừng trở lại khu vườn của bạn" : "Personal Finance Manager"}
        </p>

        {error && <div className="alert error">{error}</div>}

        <label>Email</label>
        <input type="email" value={email} required
               onChange={(e) => setEmail(e.target.value)} placeholder="demo@finance.local" />

        <label>Mật khẩu</label>
        <input type="password" value={password} required
               onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />

        <button className="btn primary" type="submit" disabled={loading}>
          {loading
            ? (garden ? "Đang mở cổng vườn..." : "Đang đăng nhập...")
            : (garden ? "Vào vườn" : "Đăng nhập")}
        </button>

        <p className="muted center">
          Chưa có tài khoản? <Link to="/register">{garden ? "Gieo hạt (đăng ký)" : "Đăng ký"}</Link>
        </p>
        <AuthDecor />
      </form>
    </div>
  );
}
