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

  async function handleDelete(id) {
    setError("");
    try {
      await deleteCategory(id);
      onChanged();
    } catch (err) {
      // Vd: xóa danh mục đang được giao dịch tham chiếu -> backend trả 409.
      setError(err.response?.data?.message || "Không xóa được (có thể đang được sử dụng)");
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
            <button className="chip-x" onClick={() => handleDelete(c.id)} title="Xóa">×</button>
          </span>
        ))}
      </div>
    </section>
  );
}
