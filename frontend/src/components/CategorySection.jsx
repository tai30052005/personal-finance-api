import { useState } from "react";
import { createCategory, deleteCategory } from "../api/finance";

// Quản lý danh mục: thêm + xóa. Danh sách lấy từ dashboard (prop categories).
export default function CategorySection({ categories, onChanged }) {
  const [name, setName] = useState("");
  const [type, setType] = useState("EXPENSE");
  const [error, setError] = useState("");

  async function handleAdd(e) {
    e.preventDefault();
    setError("");
    try {
      await createCategory({ name, type });
      setName("");
      onChanged();
    } catch (err) {
      setError(err.response?.data?.message || "Không thêm được danh mục");
    }
  }

  async function handleDelete(id, name) {
    // Xác nhận trước khi xóa — vì sẽ xóa luôn giao dịch & ngân sách của danh mục (cascade).
    const ok = window.confirm(
      `Xóa danh mục "${name}" sẽ xóa luôn TẤT CẢ giao dịch và ngân sách thuộc danh mục này.\n\nBạn chắc chắn chứ?`
    );
    if (!ok) return;

    setError("");
    try {
      await deleteCategory(id);
      onChanged();
    } catch (err) {
      setError(err.response?.data?.message || "Không xóa được danh mục");
    }
  }

  return (
    <section className="card">
      <h2>Danh mục</h2>

      <form className="inline-form" onSubmit={handleAdd}>
        <input type="text" placeholder="Tên danh mục" value={name}
               onChange={(e) => setName(e.target.value)} required />
        <select value={type} onChange={(e) => setType(e.target.value)}>
          <option value="EXPENSE">Chi (EXPENSE)</option>
          <option value="INCOME">Thu (INCOME)</option>
        </select>
        <button className="btn primary auto" type="submit">Thêm</button>
      </form>
      {error && <div className="alert error">{error}</div>}

      <div className="chips">
        {categories.map((c) => (
          <span key={c.id} className={"chip " + (c.type === "INCOME" ? "income" : "expense")}>
            {c.name}
            <button className="chip-x" onClick={() => handleDelete(c.id, c.name)} title="Xóa">×</button>
          </span>
        ))}
      </div>
    </section>
  );
}
