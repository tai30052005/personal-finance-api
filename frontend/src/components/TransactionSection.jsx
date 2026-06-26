import { useEffect, useState } from "react";
import { getTransactions, createTransaction, updateTransaction, deleteTransaction, exportTransactions } from "../api/finance";
import { formatVND } from "../utils/format";
import Modal from "./Modal";

// Danh sách giao dịch theo tháng + thêm + sửa + xóa.
export default function TransactionSection({ month, year, categories, reloadToken, onChanged }) {
  const [list, setList] = useState([]);
  const [amount, setAmount] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [note, setNote] = useState("");
  const [occurredAt, setOccurredAt] = useState(() => new Date().toISOString().slice(0, 10));
  const [error, setError] = useState("");
  // Giao dịch đang được sửa (null = không mở modal).
  const [editing, setEditing] = useState(null);

  useEffect(() => {
    let active = true;
    getTransactions({ month, year })
      .then((d) => active && setList(d))
      .catch(() => {});
    return () => { active = false; };
  }, [month, year, reloadToken]);

  async function handleAdd(e) {
    e.preventDefault();
    setError("");
    try {
      await createTransaction({
        amount: Number(amount),
        categoryId: Number(categoryId),
        note,
        occurredAt,
      });
      setAmount("");
      setNote("");
      onChanged();
    } catch (err) {
      const data = err.response?.data;
      const fe = data?.fieldErrors && Object.values(data.fieldErrors)[0];
      setError(fe || data?.message || "Không thêm được giao dịch");
    }
  }

  async function handleDelete(id) {
    await deleteTransaction(id);
    onChanged();
  }

  // Tải file CSV giao dịch của tháng đang xem.
  async function handleExport() {
    const res = await exportTransactions({ month, year });
    const url = window.URL.createObjectURL(new Blob([res.data], { type: "text/csv" }));
    const a = document.createElement("a");
    a.href = url;
    a.download = `transactions_${year}-${month}.csv`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  }

  return (
    <section className="card">
      <div className="section-head">
        <h2>Giao dịch tháng {month}/{year}</h2>
        {list.length > 0 && (
          <button className="btn auto sm" onClick={handleExport} title="Tải file CSV">⤓ Export CSV</button>
        )}
      </div>

      <form className="inline-form" onSubmit={handleAdd}>
        <input type="number" step="0.01" min="0.01" placeholder="Số tiền" value={amount}
               onChange={(e) => setAmount(e.target.value)} required />
        <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
          <option value="">-- Danh mục --</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name} ({c.type === "INCOME" ? "Thu" : "Chi"})</option>
          ))}
        </select>
        <input type="date" value={occurredAt} onChange={(e) => setOccurredAt(e.target.value)} required />
        <input type="text" placeholder="Ghi chú" value={note} onChange={(e) => setNote(e.target.value)} />
        <button className="btn primary auto" type="submit">Thêm</button>
      </form>
      {error && <div className="alert error">{error}</div>}

      {list.length === 0 ? (
        <p className="muted">Chưa có giao dịch trong tháng này.</p>
      ) : (
        <table>
          <thead>
            <tr><th>Ngày</th><th>Danh mục</th><th>Ghi chú</th><th className="right">Số tiền</th><th></th></tr>
          </thead>
          <tbody>
            {list.map((t) => (
              <tr key={t.id}>
                <td>{t.occurredAt}</td>
                <td>{t.categoryName}</td>
                <td className="muted">{t.note}</td>
                <td className={"right " + (t.categoryType === "INCOME" ? "pos" : "neg")}>
                  {formatVND(t.amount)}
                </td>
                <td className="right" style={{ whiteSpace: "nowrap" }}>
                  <button className="btn auto sm" onClick={() => setEditing(t)}>Sửa</button>{" "}
                  <button className="btn danger sm" onClick={() => handleDelete(t.id)}>Xóa</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {editing && (
        <EditTransactionModal
          tx={editing}
          categories={categories}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); onChanged(); }}
        />
      )}
    </section>
  );
}

// Form sửa giao dịch trong modal. Khởi tạo sẵn dữ liệu từ giao dịch đang chọn.
function EditTransactionModal({ tx, categories, onClose, onSaved }) {
  const [amount, setAmount] = useState(String(tx.amount));
  const [categoryId, setCategoryId] = useState(String(tx.categoryId));
  const [occurredAt, setOccurredAt] = useState(tx.occurredAt);
  const [note, setNote] = useState(tx.note || "");
  const [error, setError] = useState("");

  async function handleSave(e) {
    e.preventDefault();
    setError("");
    try {
      await updateTransaction(tx.id, {
        amount: Number(amount),
        categoryId: Number(categoryId),
        note,
        occurredAt,
      });
      onSaved();
    } catch (err) {
      const data = err.response?.data;
      const fe = data?.fieldErrors && Object.values(data.fieldErrors)[0];
      setError(fe || data?.message || "Không sửa được giao dịch");
    }
  }

  return (
    <Modal title="Sửa giao dịch" onClose={onClose}>
      <form className="modal-form" onSubmit={handleSave}>
        <label>Số tiền
          <input type="number" step="0.01" min="0.01" value={amount}
                 onChange={(e) => setAmount(e.target.value)} required />
        </label>
        <label>Danh mục
          <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name} ({c.type === "INCOME" ? "Thu" : "Chi"})</option>
            ))}
          </select>
        </label>
        <label>Ngày
          <input type="date" value={occurredAt} onChange={(e) => setOccurredAt(e.target.value)} required />
        </label>
        <label>Ghi chú
          <input type="text" value={note} onChange={(e) => setNote(e.target.value)} />
        </label>
        {error && <div className="alert error">{error}</div>}
        <div className="modal-actions">
          <button type="button" className="btn ghost auto" onClick={onClose}>Hủy</button>
          <button type="submit" className="btn primary auto">Lưu</button>
        </div>
      </form>
    </Modal>
  );
}
