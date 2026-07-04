import { useConcept, CONCEPTS } from "../theme/ConceptContext";

// Cài đặt giao diện: chọn "concept" (bộ da) + bật/tắt nền tối.
// Concept áp dụng ngay lập tức và được lưu lại cho lần sau.
export default function SettingsPage() {
  const { concept, setConcept, dark, toggleDark } = useConcept();

  return (
    <section className="card">
      <h2>⚙️ Cài đặt</h2>

      <h3 style={{ fontSize: 15, margin: "18px 0 10px" }}>Bộ giao diện (concept)</h3>
      <div className="concept-grid">
        {CONCEPTS.map((c) => (
          <button
            key={c.id}
            className={"concept-card" + (concept === c.id ? " active" : "")}
            onClick={() => setConcept(c.id)}
          >
            <span className="concept-swatch" data-c={c.id}>
              <span className="sw" /><span className="sw" /><span className="sw" />
            </span>
            <strong>{c.name}</strong>
            <span className="muted">{c.description}</span>
            {concept === c.id && <span className="badge ok" style={{ justifySelf: "start" }}>Đang dùng</span>}
          </button>
        ))}
      </div>
      <p className="muted" style={{ marginTop: 4 }}>
        Concept đổi màu sắc và một vài tên gọi trong app — dữ liệu của bạn không thay đổi.
      </p>

      <h3 style={{ fontSize: 15, margin: "20px 0 10px" }}>Chế độ hiển thị</h3>
      <div className="settings-row">
        <div>
          <strong style={{ fontSize: 14 }}>{dark ? "🌙 Nền tối đang bật" : "☀️ Nền sáng đang bật"}</strong>
          <div className="muted">Áp dụng cho concept đang chọn.</div>
        </div>
        <button className="btn auto" onClick={toggleDark}>
          {dark ? "Chuyển nền sáng" : "Chuyển nền tối"}
        </button>
      </div>
    </section>
  );
}
