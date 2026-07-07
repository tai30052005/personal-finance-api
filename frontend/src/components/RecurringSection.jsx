import { useEffect, useState } from "react";
import { getRecurring, createRecurring, updateRecurring, deleteRecurring, runRecurring } from "../api/finance";
import { formatVND } from "../utils/format";

// Giao dịch định kỳ: khai báo khoản lặp hàng tháng (tiền nhà, lương, subscription...).
// Hệ thống tự sinh giao dịch vào ngày đã đặt; có thể bấm "Ghi ngay" để tạo luôn.
export default function RecurringSection({ categories, reloadToken, onChanged }) {
  const [list, setList] = useState([]);
  const [categoryId, setCategoryId] = useState("");
  const [amount, setAmount] = useState("");
  const [dayOfMonth, setDayOfMonth] = useState("1");
  const [note, setNote] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    getRecurring().then((d) => active && setList(d)).catch(() => {});
    return () => { active = false; };
  }, [reloadToken]);

  async function handleAdd(e) {
    e.preventDefault();
    setError("");
    try {
      await createRecurring({
        categoryId: Number(categoryId),
        amount: Number(amount),
        note,
        dayOfMonth: Number(dayOfMonth),
      });
      setAmount("");
      setNote("");
      onChanged();
    } catch (err) {
      const data = err.response?.data;
      const fe = data?.fieldErrors && Object.values(data.fieldErrors)[0];
      setError(fe || data?.message || "Không thêm được khoản định kỳ");
    }
  }

  // Bật/tắt: gửi lại đầy đủ field, chỉ đảo cờ active.
  async function handleToggle(r) {
    await updateRecurring(r.id, {
      categoryId: r.categoryId, amount: r.amount, note: r.note,
      dayOfMonth: r.dayOfMonth, active: !r.active,
    });
    onChanged();
  }

  async function handleRun(id) {
    await runRecurring(id);   // tạo ngay 1 giao dịch
    onChanged();
  }

  async function handleDelete(id) {
    if (!window.confirm("Xóa khoản định kỳ này? Các giao dịch đã sinh trước đó vẫn được giữ.")) return;
    await deleteRecurring(id);
    onChanged();
  }

  return (
    <section className="card">
      <h2>Giao dịch định kỳ</h2>
      <p className="muted" style={{ marginTop: -6 }}>
        Khoản lặp hàng tháng (tiền nhà, lương, subscription…). Hệ thống tự sinh giao dịch vào ngày đã đặt.
      </p>

      <form className="inline-form" onSubmit={handleAdd}>
        <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
          <option value="">-- Danh mục --</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name} ({c.type === "INCOME" ? "Thu" : "Chi"})</option>
          ))}
        </select>
        <input type="number" step="0.01" min="0.01" placeholder="Số tiền" value={amount}
               onChange={(e) => setAmount(e.target.value)} required />
        <input type="number" min="1" max="28" placeholder="Ngày (1-28)" value={dayOfMonth}
               onChange={(e) => setDayOfMonth(e.target.value)} required title="Ngày trong tháng" />
        <input type="text" placeholder="Ghi chú" value={note} onChange={(e) => setNote(e.target.value)} />
        <button className="btn primary auto" type="submit">Thêm</button>
      </form>
      {error && <div className="alert error">{error}</div>}

      {list.length === 0 ? (
        <p className="muted">Chưa có khoản định kỳ nào.</p>
      ) : (
        <table>
          <thead>
            <tr><th>Danh mục</th><th>Ghi chú</th><th>Ngày</th><th className="right">Số tiền</th><th>Trạng thái</th><th></th></tr>
          </thead>
          <tbody>
            {list.map((r) => (
              <tr key={r.id}>
                <td>{r.categoryName}</td>
                <td className="muted">{r.note}</td>
                <td>Mỗi ngày {r.dayOfMonth}</td>
                <td className="right">{formatVND(r.amount)}</td>
                <td>
                  <span className={"badge " + (r.active ? "ok" : "danger")}>
                    {r.active ? "Đang bật" : "Đã tắt"}
                  </span>
                </td>
                <td className="right" style={{ whiteSpace: "nowrap" }}>
                  <button className="btn auto sm" onClick={() => handleRun(r.id)} title="Tạo giao dịch ngay">Ghi ngay</button>{" "}
                  <button className="btn auto sm" onClick={() => handleToggle(r)}>{r.active ? "Tắt" : "Bật"}</button>{" "}
                  <button className="btn danger sm" onClick={() => handleDelete(r.id)}>Xóa</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
