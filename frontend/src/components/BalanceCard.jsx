import { useEffect, useState } from "react";
import { getMonthlyReport } from "../api/finance";
import { formatVND } from "../utils/format";

/**
 * Thẻ số dư nền tối (concept Vườn Xanh): số dư tháng + chip thu/chi
 * + vòng "tỷ lệ tiết kiệm" (số dư / tổng thu). Điểm neo thị giác của trang.
 */
export default function BalanceCard({ month, year, reloadToken }) {
  const [report, setReport] = useState(null);

  useEffect(() => {
    let active = true;
    getMonthlyReport(month, year).then((d) => active && setReport(d)).catch(() => {});
    return () => { active = false; };
  }, [month, year, reloadToken]);

  if (!report) return <div className="balance-card" />;

  const income = Number(report.totalIncome);
  const rate = income > 0 ? Math.max(0, Math.round((Number(report.balance) / income) * 100)) : null;
  const C = 2 * Math.PI * 19;   // chu vi vòng tiến độ
  const status = rate === null ? "Chưa có thu nhập tháng này"
    : rate >= 50 ? "Vườn xanh tốt" : rate >= 20 ? "Đang bén rễ" : "Cần chăm thêm";

  return (
    <div className="balance-card">
      <div className="bc-label">Số dư tháng {month}/{year}</div>
      <div className="bc-amount">{formatVND(report.balance)}</div>
      <div className="bc-chips">
        <span className="bc-chip up">↑ Thu {formatVND(report.totalIncome)}</span>
        <span className="bc-chip down">↓ Chi {formatVND(report.totalExpense)}</span>
      </div>
      <div className="bc-ring-row">
        <svg width="52" height="52" viewBox="0 0 52 52" aria-hidden="true">
          <circle cx="26" cy="26" r="19" stroke="rgba(255,255,255,0.15)" strokeWidth="5.5" fill="none" />
          {rate !== null && (
            <circle cx="26" cy="26" r="19" stroke="#8fd6a0" strokeWidth="5.5" fill="none"
                    strokeDasharray={`${(Math.min(rate, 100) / 100) * C} ${C}`}
                    strokeLinecap="round" transform="rotate(-90 26 26)" />
          )}
          <text x="26" y="30" fontSize="12" fontWeight="600" fill="#eaf6ec" textAnchor="middle">
            {rate === null ? "—" : rate + "%"}
          </text>
        </svg>
        <div>
          <div className="bc-label">Tỷ lệ tiết kiệm</div>
          <div className="bc-status">{status}</div>
        </div>
      </div>
    </div>
  );
}
