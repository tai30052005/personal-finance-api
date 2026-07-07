import { useState, useEffect, useCallback } from "react";
import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { useConcept } from "../theme/ConceptContext";
import { getCategories } from "../api/finance";

/**
 * Khung dùng chung cho mọi trang: sidebar trái + thanh trên.
 * Giữ state DÙNG CHUNG (kỳ tháng/năm, reloadToken, categories) và truyền xuống
 * các trang con qua Outlet context, để khi chuyển trang vẫn nhớ trạng thái.
 * Brand + một vài nhãn đổi theo "concept" đang chọn (xem ConceptContext).
 */
export default function DashboardLayout() {
  const { email, logout } = useAuth();
  const { concept, dark, toggleDark } = useConcept();
  const garden = concept === "garden";

  // Nhãn concept dùng "vừa đủ": chỉ brand, trợ lý AI và nút sáng/tối đổi tên.
  const nav = [
    { to: "/", label: "Tổng quan", icon: garden ? "🌿" : "📊", end: true },
    { to: "/assistant", label: garden ? "Bác Làm Vườn" : "Trợ lý AI", icon: garden ? "🧑‍🌾" : "💬" },
    { to: "/transactions", label: "Giao dịch", icon: garden ? "📖" : "💸" },
    { to: "/budgets", label: "Ngân sách & Mục tiêu", icon: garden ? "🌱" : "🎯" },
    { to: "/recurring", label: "Định kỳ", icon: "🔁" },
    { to: "/categories", label: "Danh mục", icon: "🏷️" },
    { to: "/settings", label: "Cài đặt", icon: "⚙️" },
  ];

  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear] = useState(now.getFullYear());

  // reloadToken: mỗi lần +1 sẽ khiến các trang tải lại dữ liệu (sau khi thêm/sửa/xóa).
  const [reloadToken, setReloadToken] = useState(0);
  const [categories, setCategories] = useState([]);

  const bump = useCallback(() => setReloadToken((t) => t + 1), []);

  // Đóng/mở sidebar trên màn hình nhỏ.
  const [navOpen, setNavOpen] = useState(false);

  useEffect(() => {
    getCategories().then(setCategories).catch(() => {});
  }, [reloadToken]);

  // Không bọc useCallback: React Compiler tự memo; bọc tay ở đây còn gây cảnh báo
  // "manual memoization" vì deps suy ra (setMonth/setYear) không khớp mảng rỗng.
  const changePeriod = (m, y) => { setMonth(m); setYear(y); };

  return (
    <div className={"dash-layout" + (navOpen ? " nav-open" : "")}>
      <aside className="sidebar">
        <div className="sidebar-brand">{garden ? "🌱 Vườn Xanh" : "💰 Finance"}</div>
        <nav className="sidebar-nav">
          {nav.map((item) => (
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
        <button className="theme-toggle" onClick={toggleDark}>
          <span className="nav-icon">{dark ? "☀️" : "🌙"}</span>
          {garden ? (dark ? "Vườn sáng" : "Vườn đêm") : (dark ? "Chế độ sáng" : "Chế độ tối")}
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
