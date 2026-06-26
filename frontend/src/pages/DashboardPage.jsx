import { useState, useEffect, useCallback } from "react";
import { useAuth } from "../auth/AuthContext";
import { getCategories } from "../api/finance";
import MonthPicker from "../components/MonthPicker";
import SummarySection from "../components/SummarySection";
import InsightsSection from "../components/InsightsSection";
import ChartsSection from "../components/ChartsSection";
import BudgetSection from "../components/BudgetSection";
import GoalsSection from "../components/GoalsSection";
import TransactionSection from "../components/TransactionSection";
import RecurringSection from "../components/RecurringSection";
import CategorySection from "../components/CategorySection";

export default function DashboardPage() {
  const { email, logout } = useAuth();

  // Mặc định mở đúng tháng hiện tại.
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear] = useState(now.getFullYear());

  // reloadToken: mỗi lần +1 sẽ khiến mọi section tải lại dữ liệu (sau khi thêm/xóa).
  const [reloadToken, setReloadToken] = useState(0);
  const [categories, setCategories] = useState([]);

  const bump = useCallback(() => setReloadToken((t) => t + 1), []);

  // Tải lại danh mục mỗi khi có thay đổi.
  useEffect(() => {
    getCategories().then(setCategories).catch(() => {});
  }, [reloadToken]);

  function changePeriod(m, y) {
    setMonth(m);
    setYear(y);
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <strong>💰 Finance Manager</strong>
        <div className="topbar-right">
          <span className="muted">{email}</span>
          <button className="btn ghost" onClick={logout}>Đăng xuất</button>
        </div>
      </header>

      <main className="container">
        <MonthPicker month={month} year={year} onChange={changePeriod} />
        <SummarySection month={month} year={year} reloadToken={reloadToken} />
        <InsightsSection month={month} year={year} reloadToken={reloadToken} />
        <ChartsSection month={month} year={year} reloadToken={reloadToken} />
        <BudgetSection month={month} year={year} categories={categories}
                       reloadToken={reloadToken} onChanged={bump} />
        <GoalsSection reloadToken={reloadToken} onChanged={bump} />
        <TransactionSection month={month} year={year} categories={categories}
                            reloadToken={reloadToken} onChanged={bump} />
        <RecurringSection categories={categories} reloadToken={reloadToken} onChanged={bump} />
        <CategorySection categories={categories} onChanged={bump} />
      </main>
    </div>
  );
}
