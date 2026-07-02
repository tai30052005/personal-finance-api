import { useState, useEffect, useCallback } from "react";
import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { getCategories } from "../api/finance";

// Các mục điều hướng ở sidebar. `end` để mục "Tổng quan" (/) không bị active lan sang trang con.
const NAV = [
  { to: "/", label: "Tổng quan", icon: "📊", end: true },
  { to: "/assistant", label: "Trợ lý AI", icon: "💬" },
  { to: "/transactions", label: "Giao dịch", icon: "💸" },
  { to: "/budgets", label: "Ngân sách & Mục tiêu", icon: "🎯" },
  { to: "/recurring", label: "Định kỳ", icon: "🔁" },
  { to: "/categories", label: "Danh mục", icon: "🏷️" },
];

/**
 * Khung dùng chung cho mọi trang: sidebar trái + thanh trên.
 * Giữ state DÙNG CHUNG (kỳ tháng/năm, reloadToken, categories) và truyền xuống
 * các trang con qua Outlet context, để khi chuyển trang vẫn nhớ trạng thái.
 */
export default function DashboardLayout() {
  const { email, logout } = useAuth();

  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear] = useState(now.getFullYear());

  // reloadToken: mỗi lần +1 sẽ khiến các trang tải lại dữ liệu (sau khi thêm/sửa/xóa).
  const [reloadToken, setReloadToken] = useState(0);
  const [categories, setCategories] = useState([]);

  const bump = useCallback(() => setReloadToken((t) => t + 1), []);

  // Đóng/mở sidebar trên màn hình nhỏ.
  const [navOpen, setNavOpen] = useState(false);

  // Theme sáng/tối: đọc lựa chọn đã lưu, đổi bằng nút ở cuối sidebar.
  const [dark, setDark] = useState(() => localStorage.getItem("theme") === "dark");
  const toggleTheme = useCallback(() => {
    setDark((d) => {
      const next = !d;
      document.documentElement.dataset.theme = next ? "dark" : "";
      localStorage.setItem("theme", next ? "dark" : "light");
      return next;
    });
  }, []);

  useEffect(() => {
    getCategories().then(setCategories).catch(() => {});
  }, [reloadToken]);

  const changePeriod = useCallback((m, y) => { setMonth(m); setYear(y); }, []);

  return (
    <div className={"dash-layout" + (navOpen ? " nav-open" : "")}>
      <aside className="sidebar">
        <div className="sidebar-brand">💰 Finance</div>
        <nav className="sidebar-nav">
          {NAV.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => "nav-link" + (isActive ? " active" : "")}
              onClick={() => setNavOpen(false)}
            >
              <span className="nav-icon">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <button className="theme-toggle" onClick={toggleTheme}>
          <span className="nav-icon">{dark ? "☀️" : "🌙"}</span>
          {dark ? "Chế độ sáng" : "Chế độ tối"}
        </button>
      </aside>

      <div className="dash-main">
        <header className="topbar">
          <button className="btn ghost nav-toggle" onClick={() => setNavOpen((o) => !o)} aria-label="Menu">☰</button>
          <div className="topbar-right">
            <span className="muted">{email}</span>
            <button className="btn ghost" onClick={logout}>Đăng xuất</button>
          </div>
        </header>

        <main className="container">
          <Outlet context={{ month, year, changePeriod, reloadToken, bump, categories }} />
        </main>
      </div>

      {/* Lớp phủ khi mở sidebar trên mobile, bấm để đóng. */}
      {navOpen && <div className="nav-backdrop" onClick={() => setNavOpen(false)} />}
    </div>
  );
}
