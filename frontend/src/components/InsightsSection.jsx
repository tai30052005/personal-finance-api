import { useEffect, useState } from "react";
import { getInsights } from "../api/finance";
import { formatVND } from "../utils/format";

// Hiển thị % thay đổi so với tháng trước.
// expense: tăng = xấu (đỏ), giảm = tốt (xanh). income: ngược lại.
function ChangeBadge({ percent, goodWhenDown }) {
  if (percent === null || percent === undefined) {
    return <span className="badge ok">— (tháng trước không có dữ liệu)</span>;
  }
  const up = percent >= 0;
  const good = goodWhenDown ? !up : up;          // có lợi hay không
  const cls = good ? "ok" : "danger";
  const arrow = up ? "▲" : "▼";
  return <span className={"badge " + cls}>{arrow} {Math.abs(percent).toFixed(1)}% so với tháng trước</span>;
}

export default function InsightsSection({ month, year, reloadToken }) {
  const [data, setData] = useState(null);

  useEffect(() => {
    let active = true;
    getInsights(month, year).then((d) => active && setData(d)).catch(() => {});
    return () => { active = false; };
  }, [month, year, reloadToken]);

  if (!data) return null;

  return (
    <section className="card">
      <h2>Phân tích — tháng {month}/{year}</h2>
      <div className="insight-list">
        <div className="insight-row">
          <div>
            <span className="muted">Tổng chi</span>
            <strong className="neg"> {formatVND(data.expense)}</strong>
          </div>
          <ChangeBadge percent={data.expenseChangePercent} goodWhenDown={true} />
        </div>
        <div className="insight-row">
          <div>
            <span className="muted">Tổng thu</span>
            <strong className="pos"> {formatVND(data.income)}</strong>
          </div>
          <ChangeBadge percent={data.incomeChangePercent} goodWhenDown={false} />
        </div>
        {data.topExpenseCategory && (
          <div className="insight-row">
            <div>
              <span className="muted">Chi nhiều nhất</span>
              <strong> {data.topExpenseCategory}</strong>
            </div>
            <span className="badge danger">{formatVND(data.topExpenseAmount)}</span>
          </div>
        )}
      </div>
    </section>
  );
}
