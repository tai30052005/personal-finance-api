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

  // Bộ lọc đã áp dụng (rỗng = không lọc). Ô nhập riêng, chỉ áp dụng khi bấm "Lọc".
  const [filters, setFilters] = useState({ keyword: "", minAmount: "", maxAmount: "" });
  const [fKeyword, setFKeyword] = useState("");
  const [fMin, setFMin] = useState("");
  const [fMax, setFMax] = useState("");
  const hasFilter = filters.keyword || filters.minAmount || filters.maxAmount;

  // Gộp kỳ (tháng/năm) với bộ lọc thành tham số query (bỏ qua ô trống).
  function buildParams() {
    const p = { month, year };
    if (filters.keyword) p.keyword = filters.keyword;
    if (filters.minAmount) p.minAmount = filters.minAmount;
    if (filters.maxAmount) p.maxAmount = filters.maxAmount;
    return p;
  }

  useEffect(() => {
    let active = true;
    getTransactions(buildParams())
      .then((d) => active && setList(d))
      .catch(() => {});
    return () => { active = false; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [month, year, reloadToken, filters]);

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

  // Áp dụng bộ lọc đang gõ (kích hoạt tải lại qua useEffect).
  function handleFilter(e) {
    e.preventDefault();
    setFilters({ keyword: fKeyword.trim(), minAmount: fMin, maxAmount: fMax });
  }

  function handleClearFilter() {
    setFKeyword(""); setFMin(""); setFMax("");
    setFilters({ keyword: "", minAmount: "", maxAmount: "" });
  }

  // Tải file CSV theo đúng kỳ + bộ lọc đang xem.
  async function handleExport() {
    const res = await exportTransactions(buildParams());
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

      {/* Thanh lọc: từ khóa trong ghi chú + khoảng số tiền. */}
      <form className="inline-form" onSubmit={handleFilter}>
        <input type="text" placeholder="🔍 Tìm trong ghi chú" value={fKeyword}
               onChange={(e) => setFKeyword(e.target.value)} />
        <input type="number" step="0.01" min="0" placeholder="Tiền từ" value={fMin}
               onChange={(e) => setFMin(e.target.value)} />
        <input type="number" step="0.01" min="0" placeholder="Tiền đến" value={fMax}
               onChange={(e) => setFMax(e.target.value)} />
        <button className="btn auto" type="submit">Lọc</button>
        {hasFilter && (
          <button type="button" className="btn ghost auto" onClick={handleClearFilter}>Xóa lọc</button>
        )}
      </form>

      {list.length === 0 ? (
        <p className="muted">{hasFilter ? "Không có giao dịch khớp bộ lọc." : "Chưa có giao dịch trong tháng này."}</p>
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
