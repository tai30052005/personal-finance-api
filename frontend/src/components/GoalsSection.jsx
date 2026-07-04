import { useEffect, useState } from "react";
import { getGoals, createGoal, deleteGoal, contributeGoal } from "../api/finance";
import { formatVND } from "../utils/format";
import { useConcept } from "../theme/ConceptContext";

// Mục tiêu tiết kiệm: đặt số tiền cần đạt, góp dần, theo dõi % tiến độ.
// Concept Vườn Xanh: đổi lời — mục tiêu là "cây", thêm = "gieo hạt", góp = "tưới cây".
export default function GoalsSection({ reloadToken, onChanged }) {
  const { concept } = useConcept();
  const garden = concept === "garden";
  const [goals, setGoals] = useState([]);
  const [name, setName] = useState("");
  const [targetAmount, setTargetAmount] = useState("");
  const [deadline, setDeadline] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    getGoals().then((d) => active && setGoals(d)).catch(() => {});
    return () => { active = false; };
  }, [reloadToken]);

  async function handleAdd(e) {
    e.preventDefault();
    setError("");
    try {
      await createGoal({
        name,
        targetAmount: Number(targetAmount),
        deadline: deadline || null,
      });
      setName("");
      setTargetAmount("");
      setDeadline("");
      onChanged();
    } catch (err) {
      const data = err.response?.data;
      const fe = data?.fieldErrors && Object.values(data.fieldErrors)[0];
      setError(fe || data?.message || "Không thêm được mục tiêu");
    }
  }

  async function handleContribute(id) {
    const input = window.prompt(garden ? "Tưới bao nhiêu cho cây này?" : "Góp thêm bao nhiêu vào mục tiêu này?");
    if (input === null) return;
    const amount = Number(input);
    if (!amount || amount <= 0) { setError("Số tiền góp phải lớn hơn 0"); return; }
    setError("");
    await contributeGoal(id, amount);
    onChanged();
  }

  async function handleDelete(id) {
    await deleteGoal(id);
    onChanged();
  }

  return (
    <section className="card">
      <h2>{garden ? "🌱 Cây mục tiêu" : "Mục tiêu tiết kiệm"}</h2>

      <form className="inline-form" onSubmit={handleAdd}>
        <input type="text" placeholder="Tên mục tiêu (vd: Mua laptop)" value={name}
               onChange={(e) => setName(e.target.value)} required />
        <input type="number" step="0.01" min="0.01" placeholder="Số tiền mục tiêu" value={targetAmount}
               onChange={(e) => setTargetAmount(e.target.value)} required />
        <input type="date" value={deadline} onChange={(e) => setDeadline(e.target.value)} title="Hạn (tùy chọn)" />
        <button className="btn primary auto" type="submit">{garden ? "Gieo hạt" : "Thêm"}</button>
      </form>
      {error && <div className="alert error">{error}</div>}

      {goals.length === 0 ? (
        <p className="muted">Chưa có mục tiêu nào.</p>
      ) : (
        <div className="budget-list">
          {goals.map((g) => {
            const pct = Math.min(100, g.progressPercent);
            return (
              <div key={g.id} className={"budget-item" + (g.completed ? " done" : "")}>
                <div className="budget-head">
                  <strong>{g.name} {g.completed && (garden ? "🌸" : "✅")}</strong>
                  <span className="muted">
                    {formatVND(g.currentAmount)} / {formatVND(g.targetAmount)}
                    {g.deadline ? "  ·  hạn " + g.deadline : ""}
                  </span>
                </div>
                <div className="bar"><div className="bar-fill" style={{ width: pct + "%" }} /></div>
                <div className="section-head">
                  <span className="badge ok">{g.progressPercent.toFixed(0)}%</span>
                  <span style={{ whiteSpace: "nowrap" }}>
                    <button className="btn auto sm" onClick={() => handleContribute(g.id)}>{garden ? "🚿 Tưới cây" : "Góp thêm"}</button>{" "}
                    <button className="btn danger sm" onClick={() => handleDelete(g.id)}>Xóa</button>
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
}
