import { useEffect, useState } from "react";
import {
  PieChart, Pie, Cell, BarChart, Bar,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
} from "recharts";
import { getMonthlyReport, getYearlyReport } from "../api/finance";
import { formatVND } from "../utils/format";

// Bảng màu cho biểu đồ tròn
const COLORS = ["#2563eb", "#16a34a", "#dc2626", "#f59e0b", "#7c3aed", "#0891b2", "#db2777", "#65a30d"];

// Rút gọn số tiền trên trục: 1.500.000 -> "1.5tr", 65000 -> "65k"
function shortMoney(v) {
  if (v >= 1_000_000) return (v / 1_000_000) + "tr";
  if (v >= 1_000) return (v / 1_000) + "k";
  return "" + v;
}

export default function ChartsSection({ month, year, reloadToken }) {
  const [expenseByCategory, setExpenseByCategory] = useState([]);
  const [yearly, setYearly] = useState([]);

  useEffect(() => {
    let active = true;
    // Biểu đồ tròn: chi tiêu theo danh mục (chỉ lấy loại EXPENSE) trong tháng đang chọn
    getMonthlyReport(month, year)
      .then((r) => active && setExpenseByCategory(r.byCategory.filter((b) => b.type === "EXPENSE")))
      .catch(() => {});
    // Biểu đồ cột: thu/chi 12 tháng của năm đang chọn
    getYearlyReport(year)
      .then((r) => active && setYearly(r.months))
      .catch(() => {});
    return () => { active = false; };
  }, [month, year, reloadToken]);

  const pieData = expenseByCategory.map((b) => ({ name: b.categoryName, value: Number(b.total) }));
  const barData = yearly.map((m) => ({ thang: "T" + m.month, Thu: Number(m.income), Chi: Number(m.expense) }));

  return (
    <section className="card">
      <h2>Biểu đồ</h2>
      <div className="charts-grid">
        {/* Biểu đồ tròn — cơ cấu chi tiêu tháng */}
        <div className="chart-box">
          <h3>Chi tiêu theo danh mục — tháng {month}/{year}</h3>
          {pieData.length === 0 ? (
            <p className="muted">Chưa có chi tiêu trong tháng này.</p>
          ) : (
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie data={pieData} dataKey="value" nameKey="name" outerRadius={90} label>
                  {pieData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip formatter={(v) => formatVND(v)} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Biểu đồ cột — thu/chi cả năm */}
        <div className="chart-box">
          <h3>Thu / Chi theo tháng — năm {year}</h3>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={barData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="thang" />
              <YAxis tickFormatter={shortMoney} width={44} />
              <Tooltip formatter={(v) => formatVND(v)} />
              <Legend />
              <Bar dataKey="Thu" fill="#16a34a" />
              <Bar dataKey="Chi" fill="#dc2626" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </section>
  );
}
