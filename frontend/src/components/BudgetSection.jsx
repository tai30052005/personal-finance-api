import { useEffect, useState } from "react";
import { getBudgetStatus, setBudget } from "../api/finance";
import { formatVND } from "../utils/format";

// Ngân sách tháng + CẢNH BÁO VƯỢT (thanh đỏ khi over budget).
export default function BudgetSection({ month, year, categories, reloadToken, onChanged }) {
  const [status, setStatus] = useState([]);
  const [categoryId, setCategoryId] = useState("");
  const [amountLimit, setAmountLimit] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    getBudgetStatus(month, year)
      .then((d) => active && setStatus(d))
      .catch(() => {});
    return () => { active = false; };
  }, [month, year, reloadToken]);

  // Chỉ cho đặt ngân sách cho danh mục CHI (EXPENSE).
  const expenseCats = categories.filter((c) => c.type === "EXPENSE");

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    try {
      await setBudget({
        categoryId: Number(categoryId),
        amountLimit: Number(amountLimit),
        month,
        year,
      });
      setCategoryId("");
      setAmountLimit("");
      onChanged();   // báo cho dashboard refresh toàn bộ
    } catch (err) {
      setError(err.response?.data?.message || "Không đặt được ngân sách");
    }
  }

  return (
    <section className="card">
      <h2>Ngân sách tháng {month}/{year}</h2>

      {status.length === 0 && <p className="muted">Chưa có ngân sách nào cho tháng này.</p>}

      <div className="budget-list">
        {status.map((b) => {
          const limit = Number(b.amountLimit);
          const pct = limit ? Math.min(100, (Number(b.spent) / limit) * 100) : 0;
          return (
            <div key={b.budgetId} className={"budget-item" + (b.isOverBudget ? " over" : "")}>
              <div className="budget-head">
                <strong>{b.categoryName}</strong>
                <span className="muted">{formatVND(b.spent)} / {formatVND(b.amountLimit)}</span>
              </div>
              <div className="bar"><div className="bar-fill" style={{ width: pct + "%" }} /></div>
              {b.isOverBudget ? (
                <span className="badge danger">⚠ Vượt {formatVND(b.overspentAmount)}</span>
              ) : (
                <span className="badge ok">Còn lại {formatVND(b.remaining)}</span>
              )}
            </div>
          );
        })}
      </div>

      <form className="inline-form" onSubmit={handleSubmit}>
        <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
          <option value="">-- Danh mục chi --</option>
          {expenseCats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <input type="number" min="1" placeholder="Hạn mức" value={amountLimit}
               onChange={(e) => setAmountLimit(e.target.value)} required />
        <button className="btn primary auto" type="submit">Đặt ngân sách</button>
      </form>
      {error && <div className="alert error">{error}</div>}
    </section>
  );
}
