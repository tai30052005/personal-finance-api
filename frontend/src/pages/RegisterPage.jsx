import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { useConcept } from "../theme/ConceptContext";
import AuthDecor from "../components/AuthDecor";

export default function RegisterPage() {
  const { concept } = useConcept();
  const garden = concept === "garden";
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
        <h1>{garden ? "🌱 Vườn Xanh" : "💰 Finance"}</h1>
        <p className="muted">
          {garden ? "Gieo hạt đầu tiên — bắt đầu khu vườn tài chính của bạn" : "Tạo tài khoản mới"}
        </p>

        {error && <div className="alert error">{error}</div>}

        <label>Email</label>
        <input type="email" value={email} required
               onChange={(e) => setEmail(e.target.value)} placeholder="ban@example.com" />

        <label>Mật khẩu (tối thiểu 6 ký tự)</label>
        <input type="password" value={password} required minLength={6}
               onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />

        <button className="btn primary" type="submit" disabled={loading}>
          {loading
            ? (garden ? "Đang dựng vườn..." : "Đang tạo...")
            : (garden ? "Tạo khu vườn" : "Đăng ký")}
        </button>

        <p className="muted center">
          Đã có tài khoản? <Link to="/login">{garden ? "Vào vườn" : "Đăng nhập"}</Link>
        </p>
        <AuthDecor />
      </form>
    </div>
  );
}
