// Chọn tháng/năm — điều khiển chung cho báo cáo, ngân sách và danh sách giao dịch.
export default function MonthPicker({ month, year, onChange }) {
  return (
    <section className="card month-picker">
      <strong>Kỳ:</strong>
      <label>
        Tháng
        <select value={month} onChange={(e) => onChange(Number(e.target.value), year)}>
          {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
            <option key={m} value={m}>{m}</option>
          ))}
        </select>
      </label>
      <label>
        Năm
        <input
          type="number"
          value={year}
          onChange={(e) => onChange(month, Number(e.target.value))}
        />
      </label>
    </section>
  );
}
