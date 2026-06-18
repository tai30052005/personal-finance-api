import { useEffect, useState } from "react";
import { getMonthlyReport } from "../api/finance";
import { formatVND } from "../utils/format";

// Báo cáo tháng: tổng thu / chi / số dư + phân tích theo danh mục.
export default function SummarySection({ month, year, reloadToken }) {
  const [report, setReport] = useState(null);

  // useEffect: chạy lại mỗi khi month/year/reloadToken đổi -> gọi API lấy báo cáo.
  useEffect(() => {
    let active = true;
    getMonthlyReport(month, year)
      .then((d) => active && setReport(d))
      .catch(() => {});
    return () => { active = false; };   // tránh set state khi component đã unmount
  }, [month, year, reloadToken]);

  if (!report) return null;

  return (
    <section className="card">
      <h2>Báo cáo tháng {month}/{year}</h2>
      <div className="stat-grid">
        <div className="stat">
          <span className="muted">Tổng thu</span>
          <strong className="pos">{formatVND(report.totalIncome)}</strong>
        </div>
        <div className="stat">
          <span className="muted">Tổng chi</span>
          <strong className="neg">{formatVND(report.totalExpense)}</strong>
        </div>
        <div className="stat">
          <span className="muted">Số dư</span>
          <strong>{formatVND(report.balance)}</strong>
        </div>
      </div>

      {report.byCategory.length > 0 && (
        <table>
          <thead>
            <tr><th>Danh mục</th><th>Loại</th><th className="right">Tổng</th></tr>
          </thead>
          <tbody>
            {report.byCategory.map((b) => (
              <tr key={b.categoryId}>
                <td>{b.categoryName}</td>
                <td>{b.type === "INCOME" ? "Thu" : "Chi"}</td>
                <td className="right">{formatVND(b.total)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
