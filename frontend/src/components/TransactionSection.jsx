import { useEffect, useState } from "react";
import { getTransactions, createTransaction, updateTransaction, deleteTransaction, exportTransactions, parseTransaction, isParseEnabled, createCategory } from "../api/finance";
import { formatVND, categoryColor } from "../utils/format";
import Modal from "./Modal";
import ReceiptUpload from "./ReceiptUpload";

// Danh sách giao dịch theo tháng + thêm + sửa + xóa.
export default function TransactionSection({ month, year, categories, reloadToken, onChanged }) {
  const [list, setList] = useState([]);
  const [amount, setAmount] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [note, setNote] = useState("");
  const [occurredAt, setOccurredAt] = useState(() => new Date().toISOString().slice(0, 10));
  const [receiptUrl, setReceiptUrl] = useState("");
  const [error, setError] = useState("");
  // Giao dịch đang được sửa (null = không mở modal).
  const [editing, setEditing] = useState(null);

  // AI: nhập nhanh bằng ngôn ngữ tự nhiên.
  const [aiEnabled, setAiEnabled] = useState(false);
  const [quickText, setQuickText] = useState("");
  const [parsing, setParsing] = useState(false);
  const [aiHint, setAiHint] = useState("");
  // Danh mục AI gợi ý nhưng CHƯA CÓ ({name, type}) -> hiện nút "+ Tạo danh mục này".
  const [suggestedCat, setSuggestedCat] = useState(null);
  // Khi AI tách >1 giao dịch: danh sách chờ duyệt rồi "Thêm tất cả".
  const [batch, setBatch] = useState(null);
  const [batchKey, setBatchKey] = useState(0);   // đổi mỗi lần parse -> remount BatchPreview

  useEffect(() => {
    isParseEnabled().then(setAiEnabled).catch(() => setAiEnabled(false));
  }, []);

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
        receiptUrl: receiptUrl || null,
      });
      setAmount("");
      setNote("");
      setReceiptUrl("");
      onChanged();
    } catch (err) {
      const data = err.response?.data;
      const fe = data?.fieldErrors && Object.values(data.fieldErrors)[0];
      setError(fe || data?.message || "Không thêm được giao dịch");
    }
  }

  async function handleDelete(id) {
    if (!window.confirm("Xóa giao dịch này? Thao tác không thể hoàn tác.")) return;
    await deleteTransaction(id);
    onChanged();
  }

  // Gửi câu chữ cho Claude phân tích rồi điền sẵn các ô của form thêm.
  async function handleQuickParse(e) {
    e.preventDefault();
    if (!quickText.trim()) return;
    setError("");
    setAiHint("");
    setSuggestedCat(null);
    setParsing(true);
    try {
      const results = await parseTransaction(quickText.trim());
      // Nhiều giao dịch -> mở bảng "Thêm tất cả"; 1 giao dịch -> điền sẵn form như cũ.
      if (results.length > 1) {
        setBatch(results);
        setBatchKey((k) => k + 1);
        setQuickText("");
        return;
      }
      const p = results[0];
      if (p.amount != null) setAmount(String(p.amount));
      if (p.occurredAt) setOccurredAt(p.occurredAt);
      if (p.note != null) setNote(p.note);
      setCategoryId(p.categoryId != null ? String(p.categoryId) : "");

      // Thiếu tiền -> nhắc; danh mục chưa có -> để dành cho nút "+ Tạo danh mục này".
      setAiHint(p.amount == null ? "⚠️ Chưa nhận ra số tiền — hãy nhập số tiền." : "");
      setSuggestedCat(p.categoryId == null && p.categoryName ? { name: p.categoryName, type: p.type } : null);
      setQuickText("");
    } catch (err) {
      const data = err.response?.data;
      setError(data?.message || "Không phân tích được, hãy thử lại hoặc nhập tay.");
    } finally {
      setParsing(false);
    }
  }

  // Tạo nhanh danh mục AI gợi ý rồi chọn luôn cho form.
  async function handleCreateSuggested() {
    if (!suggestedCat) return;
    try {
      const c = await createCategory({ name: suggestedCat.name, type: suggestedCat.type });
      setCategoryId(String(c.id));
      setSuggestedCat(null);
      onChanged();   // tải lại danh sách danh mục để select có lựa chọn mới
    } catch (err) {
      setError(err.response?.data?.message || "Không tạo được danh mục");
    }
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

      {aiEnabled && (
        <form className="inline-form quick-form" onSubmit={handleQuickParse}>
          <input type="text" className="quick-input" placeholder='⚡ Nhập nhanh, vd: "cà phê 35k hôm qua"'
                 value={quickText} onChange={(e) => setQuickText(e.target.value)} />
          <button className="btn auto" type="submit" disabled={parsing}>
            {parsing ? "Đang phân tích..." : "Phân tích"}
          </button>
        </form>
      )}
      {aiHint && <div className="alert hint">{aiHint}</div>}
      {suggestedCat && (
        <div className="alert hint suggest-cat">
          <span>AI gợi ý danh mục "<strong>{suggestedCat.name}</strong>" ({suggestedCat.type === "INCOME" ? "Thu" : "Chi"}) — chưa có trong danh sách.</span>
          <button type="button" className="btn auto sm" onClick={handleCreateSuggested}>+ Tạo danh mục này</button>
        </div>
      )}

      {batch && (
        <BatchPreview
          key={batchKey}
          items={batch}
          categories={categories}
          onCreateCategory={async (name, type) => await createCategory({ name, type })}
          onCancel={() => setBatch(null)}
          onDone={() => { setBatch(null); onChanged(); }}
        />
      )}

      <form className="inline-form" onSubmit={handleAdd}>
        <input type="number" step="0.01" min="0.01" placeholder="Số tiền" value={amount}
               onChange={(e) => setAmount(e.target.value)} required />
        <select value={categoryId} onChange={(e) => { setCategoryId(e.target.value); setSuggestedCat(null); }} required>
          <option value="">-- Danh mục --</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name} ({c.type === "INCOME" ? "Thu" : "Chi"})</option>
          ))}
        </select>
        <input type="date" value={occurredAt} onChange={(e) => setOccurredAt(e.target.value)} required />
        <input type="text" placeholder="Ghi chú" value={note} onChange={(e) => setNote(e.target.value)} />
        <ReceiptUpload value={receiptUrl} onChange={setReceiptUrl} />
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
                <td>
                  <span className="cat-chip">
                    <span className="cat-dot" style={{ background: categoryColor(t.categoryName) }} />
                    {t.categoryName}
                  </span>
                </td>
                <td className="muted">
                  {t.note}
                  {t.receiptUrl && (
                    <a href={t.receiptUrl} target="_blank" rel="noreferrer" title="Xem hóa đơn">
                      <img src={t.receiptUrl} alt="Hóa đơn" className="receipt-thumb" />
                    </a>
                  )}
                </td>
                <td className={"right amount " + (t.categoryType === "INCOME" ? "pos" : "neg")}>
                  {t.categoryType === "INCOME" ? "+" : "−"}{formatVND(t.amount)}
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
  const [receiptUrl, setReceiptUrl] = useState(tx.receiptUrl || "");
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
        receiptUrl: receiptUrl || null,
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
        <label>Ảnh hóa đơn
          <ReceiptUpload value={receiptUrl} onChange={setReceiptUrl} />
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

// Bảng xem trước khi AI tách ra NHIỀU giao dịch: sửa nhanh rồi "Thêm tất cả".
function BatchPreview({ items, categories, onCreateCategory, onCancel, onDone }) {
  const today = () => new Date().toISOString().slice(0, 10);
  const [rows, setRows] = useState(() =>
    items.map((p) => ({
      amount: p.amount != null ? String(p.amount) : "",
      categoryId: p.categoryId != null ? String(p.categoryId) : "",
      occurredAt: p.occurredAt || today(),
      note: p.note || "",
      suggested: p.categoryId == null ? p.categoryName : null,
      suggestedType: p.type,
    }))
  );
  const [created, setCreated] = useState([]);   // danh mục tạo tại chỗ trong batch
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const allCats = [...categories, ...created];

  const update = (i, field, val) =>
    setRows((rs) => rs.map((r, idx) => (idx === i ? { ...r, [field]: val } : r)));
  const removeRow = (i) => setRows((rs) => rs.filter((_, idx) => idx !== i));

  // Tạo nhanh danh mục AI gợi ý cho dòng i rồi chọn luôn.
  async function createFor(i) {
    const r = rows[i];
    if (!r.suggested) return;
    try {
      const c = await onCreateCategory(r.suggested, r.suggestedType);
      setCreated((cs) => [...cs, c]);
      setRows((rs) => rs.map((row, idx) => (idx === i ? { ...row, categoryId: String(c.id), suggested: null } : row)));
    } catch {
      setError("Không tạo được danh mục.");
    }
  }

  async function addAll() {
    const valid = rows.filter((r) => Number(r.amount) > 0 && r.categoryId);
    if (valid.length === 0) {
      setError("Chưa dòng nào đủ số tiền và danh mục.");
      return;
    }
    setError("");
    setSaving(true);
    try {
      for (const r of valid) {
        await createTransaction({
          amount: Number(r.amount),
          categoryId: Number(r.categoryId),
          note: r.note,
          occurredAt: r.occurredAt,
        });
      }
      onDone();
    } catch {
      setError("Có lỗi khi thêm, hãy thử lại.");
    } finally {
      setSaving(false);
    }
  }

  if (rows.length === 0) return null;

  return (
    <div className="batch-preview">
      <div className="section-head">
        <strong>⚡ AI tách được {rows.length} giao dịch — kiểm tra rồi thêm</strong>
        <button className="btn ghost auto sm" onClick={onCancel}>Hủy</button>
      </div>
      {rows.map((r, i) => (
        <div className="batch-row" key={i}>
          <input type="number" step="0.01" min="0.01" placeholder="Số tiền" value={r.amount}
                 onChange={(e) => update(i, "amount", e.target.value)} />
          <select value={r.categoryId} onChange={(e) => update(i, "categoryId", e.target.value)}>
            <option value="">{r.suggested ? `-- ${r.suggested}? --` : "-- Danh mục --"}</option>
            {allCats.map((c) => (
              <option key={c.id} value={c.id}>{c.name} ({c.type === "INCOME" ? "Thu" : "Chi"})</option>
            ))}
          </select>
          {!r.categoryId && r.suggested && (
            <button className="btn auto sm" onClick={() => createFor(i)} title={`Tạo danh mục "${r.suggested}"`}>+ Tạo</button>
          )}
          <input type="date" value={r.occurredAt} onChange={(e) => update(i, "occurredAt", e.target.value)} />
          <input type="text" placeholder="Ghi chú" value={r.note} onChange={(e) => update(i, "note", e.target.value)} />
          <button className="btn danger sm auto" onClick={() => removeRow(i)} title="Bỏ dòng">✕</button>
        </div>
      ))}
      {error && <div className="alert error">{error}</div>}
      <div className="modal-actions">
        <button className="btn primary auto" onClick={addAll} disabled={saving}>
          {saving ? "Đang thêm..." : `Thêm tất cả (${rows.length})`}
        </button>
      </div>
    </div>
  );
}
