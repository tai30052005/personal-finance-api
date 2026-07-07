import { useEffect, useState } from "react";
import { getActivity, getGoals } from "../api/finance";

/**
 * "Chăm vườn đều tay" (concept Vườn Xanh):
 *  - Heatmap 28 ngày: mỗi ô một ngày, càng đậm càng ghi chép nhiều (kiểu GitHub).
 *  - Chuỗi ngày liên tiếp có ghi chép (streak) tính đến hôm nay.
 *  - Huy hiệu SUY RA từ dữ liệu thật — không cần bảng DB riêng.
 */
const DAY_MS = 24 * 60 * 60 * 1000;
// Ngày dạng YYYY-MM-DD theo GIỜ ĐỊA PHƯƠNG (không dùng toISOString vì nó quy về UTC,
// người dùng UTC+7 ghi buổi tối sẽ bị tính lệch sang ngày hôm sau).
const iso = (d) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;

export default function GardenStreakCard({ reloadToken }) {
  const [activity, setActivity] = useState(null);
  const [goals, setGoals] = useState([]);

  useEffect(() => {
    let active = true;
    getActivity(28).then((d) => active && setActivity(d)).catch(() => active && setActivity([]));
    getGoals().then((d) => active && setGoals(d)).catch(() => {});
    return () => { active = false; };
  }, [reloadToken]);

  if (activity === null) return null;

  const byDate = Object.fromEntries(activity.map((a) => [a.date, a.count]));
  const today = new Date();

  // 28 ô, cũ -> mới (ô cuối là hôm nay).
  const cells = [];
  for (let i = 27; i >= 0; i--) {
    const d = new Date(today.getTime() - i * DAY_MS);
    const count = byDate[iso(d)] || 0;
    cells.push({ key: iso(d), level: count === 0 ? 0 : count === 1 ? 1 : count <= 3 ? 2 : 3 });
  }

  // Chuỗi ngày liên tiếp có ghi chép, đếm lùi từ hôm nay.
  let streak = 0;
  for (let i = 0; ; i++) {
    const d = new Date(today.getTime() - i * DAY_MS);
    if (byDate[iso(d)] > 0) streak++;
    else break;
  }

  const totalLogs = activity.reduce((s, a) => s + a.count, 0);
  const harvested = goals.some((g) => g.completed || (g.progressPercent || 0) >= 100);

  const badges = [
    { icon: "🔥", name: `Chuỗi ${streak} ngày`, sub: "ghi chép liên tục", on: streak >= 3 },
    { icon: "🌸", name: "Vụ mùa đầu tiên", sub: "có cây đã hái quả", on: harvested },
    { icon: "📒", name: "Chăm ghi chép", sub: "15+ lần trong 28 ngày", on: totalLogs >= 15 },
  ];

  return (
    <div className="garden-row2">
      <section className="card">
        <div className="section-head">
          <h2 style={{ fontSize: 16, margin: 0 }}>Chăm vườn đều tay</h2>
          <span className="badge ok">🔥 {streak} ngày liên tục</span>
        </div>
        <div className="hm-grid" style={{ marginTop: 12 }}>
          {cells.map((c) => <span key={c.key} className={`hm-cell l${c.level}`} title={c.key} />)}
        </div>
        <p className="muted" style={{ marginTop: 8, marginBottom: 0, fontSize: 12 }}>
          28 ngày gần nhất · ô đậm = ngày ghi chép nhiều
        </p>
      </section>

      <section className="card">
        <h2 style={{ fontSize: 16 }}>Huy hiệu vườn</h2>
        <div className="badge-row">
          {badges.map((b) => (
            <div key={b.name} className={"gbadge" + (b.on ? "" : " off")}>
              <span className="gbadge-ico">{b.icon}</span>
              <div className="gbadge-name">{b.name}</div>
              <div className="gbadge-sub">{b.sub}</div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
