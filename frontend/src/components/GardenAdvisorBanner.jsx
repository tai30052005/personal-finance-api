import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getRecurring, getBudgetStatus } from "../api/finance";
import { formatVND } from "../utils/format";

/**
 * Banner "Bác Làm Vườn nhắn" (concept Vườn Xanh) — bản tin dự báo NHỎ trên Tổng quan.
 * Tính bằng LOGIC THUẦN (không tốn lượt AI):
 *   - "mưa to"  : các luống (ngân sách) đã vượt hạn mức trong kỳ đang xem
 *   - "áp thấp" : khoản chi định kỳ (tưới tự động) tới hạn trong 7 ngày tới
 * Nút "Hỏi bác" dẫn sang trang chat để hỏi sâu hơn.
 */
export default function GardenAdvisorBanner({ month, year, reloadToken }) {
  const [message, setMessage] = useState(null);

  useEffect(() => {
    let active = true;
    Promise.all([
      getRecurring().catch(() => []),
      getBudgetStatus(month, year).catch(() => []),
    ]).then(([recurring, budgets]) => {
      if (!active) return;

      const today = new Date();
      const day = today.getDate();
      const daysInMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0).getDate();
      // Tới hạn trong 7 ngày tới (tính cả trường hợp vắt sang đầu tháng sau).
      const dueSoon = (recurring || []).filter((r) => {
        if (!r.active) return false;
        const d = r.dayOfMonth;
        const upper = day + 7;
        return (d >= day && d <= upper) || (upper > daysInMonth && d <= upper - daysInMonth);
      });
      const over = (budgets || []).filter((b) => b.isOverBudget);

      const parts = [];
      if (over.length > 0) {
        parts.push(`mưa to trên luống ${over.map((b) => b.categoryName).join(", ")} — đã vượt hạn mức, che chắn lại nhé`);
      }
      if (dueSoon.length > 0) {
        const total = dueSoon.reduce((s, r) => s + Number(r.amount), 0);
        parts.push(`áp thấp sắp về: ${dueSoon.length} khoản tưới tự động tới hạn trong 7 ngày (${formatVND(total)})`);
      }
      setMessage(parts.length > 0
        ? "Dự báo: " + parts.join("; ") + "."
        : "Trời quang mây tạnh — vườn đang yên ả, cứ chăm đều tay nhé!");
    });
    return () => { active = false; };
  }, [month, year, reloadToken]);

  if (!message) return null;

  return (
    <div className="advisor-banner">
      <span className="ab-ico">🧑‍🌾</span>
      <div className="ab-body">
        <div className="ab-title">Bác Làm Vườn nhắn</div>
        <div className="ab-text">{message}</div>
      </div>
      <Link to="/assistant" className="btn auto sm">Hỏi bác →</Link>
    </div>
  );
}
