import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getGoals, getBudgetStatus } from "../api/finance";
import { formatVND } from "../utils/format";
import { useConcept } from "../theme/ConceptContext";

/**
 * Hero "Khu vườn" (concept Vườn Xanh): mỗi mục tiêu tiết kiệm là một cái cây,
 * vẽ bằng SVG và LỚN THEO % TIẾN ĐỘ THẬT — không dùng ảnh tĩnh.
 *   < 25%  : mầm non      | 25–60% : bụi lá
 *   60–99% : cây đơm nụ   | 100%   : nở hoa (nhãn vàng "hái quả")
 * Chip thời tiết = tình hình ngân sách tháng (quang / mây / mưa).
 * Bật "Vườn đêm" (dark mode) -> cảnh chuyển sang ĐÊM: trăng, sao, đom đóm.
 */

// Bảng màu cây theo ngày/đêm (đêm = tông trầm như dưới ánh trăng).
const DAY = { g1: "#569148", g2: "#66a863", g3: "#7bb868", g4: "#82c177", mound: "#6fa658", trunk: "#6b4a2e", stem: "#3e6b3a" };
const NIGHT = { g1: "#39664a", g2: "#447a57", g3: "#528a63", g4: "#5f9a70", mound: "#3c5c48", trunk: "#4a3521", stem: "#2c4a38" };

function Sprout({ c }) {
  return (
    <>
      <ellipse cx="0" cy="0" rx="12" ry="3" fill={c.mound} />
      <path d="M0 0 C0 -5 0 -9 0 -13" stroke={c.stem} strokeWidth="2.5" fill="none" strokeLinecap="round" />
      <path d="M0 -11 C-8 -13 -11 -19 -9 -24 C-3 -21 0 -17 0 -11 Z" fill={c.g3} />
      <path d="M0 -11 C8 -13 11 -19 9 -24 C3 -21 0 -17 0 -11 Z" fill={c.g1} />
    </>
  );
}

function Bush({ c }) {
  return (
    <>
      <ellipse cx="0" cy="0" rx="17" ry="4" fill={c.mound} />
      <path d="M0 0 C-7 -9 -11 -19 -9 -31 C-1 -24 2 -12 0 0 Z" fill={c.g1} />
      <path d="M0 0 C7 -9 11 -19 9 -31 C1 -24 -2 -12 0 0 Z" fill={c.g3} />
      <path d="M0 0 C-4 -12 -4 -25 0 -37 C4 -25 4 -12 0 0 Z" fill={c.g2} />
      <path d="M0 0 C-10 -5 -16 -13 -17 -22 C-8 -18 -2 -9 0 0 Z" fill={c.g3} />
      <path d="M0 0 C10 -5 16 -13 17 -22 C8 -18 2 -9 0 0 Z" fill={c.g1} />
    </>
  );
}

function Tree({ c, bloomed }) {
  return (
    <>
      <ellipse cx="0" cy="0" rx="20" ry="4.5" fill={c.mound} />
      <path d="M0 0 C0 -18 -3 -38 0 -62" stroke={c.trunk} strokeWidth="5" fill="none" strokeLinecap="round" />
      <path d="M0 -34 C-7 -39 -14 -42 -19 -42" stroke={c.trunk} strokeWidth="3" fill="none" strokeLinecap="round" />
      <path d="M0 -48 C7 -53 14 -56 19 -56" stroke={c.trunk} strokeWidth="3" fill="none" strokeLinecap="round" />
      <circle cx="0" cy="-84" r="26" fill={c.g1} />
      <circle cx="-17" cy="-95" r="17" fill={c.g2} />
      <circle cx="18" cy="-97" r="15" fill={c.g3} />
      <circle cx="0" cy="-108" r="14" fill={c.g4} />
      {bloomed ? (
        <>
          <circle cx="-15" cy="-100" r="4" fill="#f4a9c4" />
          <circle cx="14" cy="-104" r="4" fill="#f4a9c4" />
          <circle cx="21" cy="-86" r="4" fill="#f4a9c4" />
          <circle cx="-22" cy="-82" r="4" fill="#f4a9c4" />
          <circle cx="2" cy="-92" r="4" fill="#f4a9c4" />
          <circle cx="-4" cy="-114" r="3.5" fill="#f6c453" />
          <circle cx="16" cy="-78" r="3.5" fill="#f6c453" />
        </>
      ) : (
        <>
          <ellipse cx="-12" cy="-102" rx="3.6" ry="4.8" fill="#efb8ce" />
          <ellipse cx="12" cy="-95" rx="3.6" ry="4.8" fill="#efb8ce" />
          <ellipse cx="1" cy="-116" rx="3.6" ry="4.8" fill="#efb8ce" />
        </>
      )}
    </>
  );
}

function Plant({ x, base, goal, night }) {
  const c = night ? NIGHT : DAY;
  const pct = Math.round(goal.progressPercent || 0);
  const p = Math.min(pct / 100, 1);
  const done = goal.completed || pct >= 100;

  let body, scale;
  if (p < 0.25) { scale = 0.8 + p * 1.6; body = <Sprout c={c} />; }
  else if (p < 0.6) { scale = 0.75 + (p - 0.25) * 0.8; body = <Bush c={c} />; }
  else { scale = 0.8 + (p - 0.6) * 0.5; body = <Tree c={c} bloomed={done} />; }

  const name = goal.name.length > 10 ? goal.name.slice(0, 9) + "…" : goal.name;
  const label = done ? `${name} · hái quả!` : `${name} · ${pct}%`;
  const w = label.length * 6.2 + 18;
  const pillFill = done ? "#f2c24e" : night ? "rgba(16,28,20,0.85)" : "#ffffff";
  const pillText = done ? "#5c3d08" : night ? "#cfe8d4" : "#3f6b34";

  return (
    <g transform={`translate(${x} ${base})`}>
      <g transform={`scale(${scale})`}>{body}</g>
      <rect x={-w / 2} y="8" width={w} height="18" rx="9" fill={pillFill} opacity={done ? 1 : 0.92} />
      <text x="0" y="21" fontSize="11" fontWeight="600" textAnchor="middle" fill={pillText}>{label}</text>
    </g>
  );
}

// Đom đóm: chấm vàng nhấp nháy (SVG animate, chỉ hiện ở Vườn đêm).
function Firefly({ x, y, delay }) {
  return (
    <circle cx={x} cy={y} r="2.2" fill="#f6d453">
      <animate attributeName="opacity" values="0.15;1;0.15" dur="2.6s" begin={`${delay}s`} repeatCount="indefinite" />
    </circle>
  );
}

// Chip thời tiết từ % ngân sách đã dùng của tháng (logic thuần, không gọi AI).
function weatherFrom(status) {
  const withLimit = (status || []).filter((b) => Number(b.amountLimit) > 0);
  if (withLimit.length === 0) return null;
  const spent = withLimit.reduce((s, b) => s + Number(b.spent), 0);
  const limit = withLimit.reduce((s, b) => s + Number(b.amountLimit), 0);
  const pct = Math.round((spent / limit) * 100);
  if (pct > 100) return { icon: "🌧️", text: `Mưa to · đã vượt ${pct}% ngân sách`, wet: true };
  if (pct > 70) return { icon: "⛅", text: `Có mây · đã dùng ${pct}% ngân sách`, wet: false };
  return { icon: "☀️", text: `Trời quang · mới dùng ${pct}% ngân sách`, wet: false };
}

// Vị trí cây theo số lượng (tối đa 4 cây, xếp theo tiến độ tăng dần).
const SLOTS = { 1: [300], 2: [200, 400], 3: [130, 300, 470], 4: [95, 235, 375, 515] };

export default function GardenHero({ month, year, reloadToken }) {
  const { dark: night } = useConcept();
  const [goals, setGoals] = useState(null);
  const [weather, setWeather] = useState(null);

  useEffect(() => {
    let active = true;
    getGoals().then((d) => active && setGoals(d)).catch(() => active && setGoals([]));
    getBudgetStatus(month, year)
      .then((d) => active && setWeather(weatherFrom(d)))
      .catch(() => {});
    return () => { active = false; };
  }, [month, year, reloadToken]);

  if (goals === null) return <div className="garden-hero" />;

  const shown = [...goals].sort((a, b) => (a.progressPercent || 0) - (b.progressPercent || 0)).slice(0, 4);
  const xs = SLOTS[shown.length] || [];
  const total = goals.reduce((s, g) => s + Number(g.currentAmount || 0), 0);

  return (
    <div className={"garden-hero" + (night ? " night" : "")}>
      <svg viewBox="0 0 600 236" preserveAspectRatio="xMidYMax slice" aria-hidden="true">
        <rect width="600" height="236" fill={night ? "#0e1a2b" : "#d9edf7"} />
        {night ? (
          <>
            <circle cx="540" cy="42" r="24" fill="#e8e6d8" opacity="0.18" />
            <circle cx="540" cy="42" r="15" fill="#e8e6d8" />
            <circle cx="534" cy="38" r="3.5" fill="#cfccba" opacity="0.7" />
            <circle cx="546" cy="47" r="2.5" fill="#cfccba" opacity="0.7" />
            <g fill="#ffffff">
              <circle cx="80" cy="36" r="1.4" opacity="0.9" /><circle cx="150" cy="58" r="1.1" opacity="0.7" />
              <circle cx="240" cy="30" r="1.5" opacity="0.8" /><circle cx="330" cy="52" r="1.1" opacity="0.6" />
              <circle cx="420" cy="26" r="1.4" opacity="0.9" /><circle cx="470" cy="70" r="1.1" opacity="0.7" />
              <circle cx="200" cy="80" r="1" opacity="0.5" /><circle cx="370" cy="90" r="1" opacity="0.5" />
            </g>
          </>
        ) : (
          <>
            <circle cx="540" cy="42" r="24" fill="#f2c24e" opacity="0.35" />
            <circle cx="540" cy="42" r="15" fill="#f2c24e" />
            <g fill="#ffffff" opacity="0.9">
              <ellipse cx="352" cy="34" rx="22" ry="8" /><ellipse cx="368" cy="28" rx="14" ry="7" />
              <ellipse cx="452" cy="66" rx="16" ry="6" /><ellipse cx="463" cy="62" rx="10" ry="5" />
            </g>
            <path d="M356 92 C360 86 366 86 368 90 C370 86 376 86 378 92 C374 98 362 98 356 92 Z" fill="#e88aae" />
          </>
        )}
        <path d="M0 156 C90 128 210 150 330 132 C420 120 520 132 600 122 L600 236 L0 236 Z" fill={night ? "#22432f" : "#b9dca0"} />
        <path d="M0 186 C110 160 240 180 370 164 C460 154 540 162 600 156 L600 236 L0 236 Z" fill={night ? "#1a3524" : "#8fc276"} />
        <path d="M18 166 L18 202 M18 174 L42 166 M18 188 L42 180" stroke={night ? "#6b532f" : "#a8763e"} strokeWidth="4" strokeLinecap="round" />
        {shown.map((g, i) => (
          <Plant key={g.id} x={xs[i]} base={200 - i * 7} goal={g} night={night} />
        ))}
        {night && (
          <>
            <Firefly x={160} y={150} delay={0} />
            <Firefly x={320} y={120} delay={0.9} />
            <Firefly x={430} y={140} delay={1.7} />
            <Firefly x={70} y={130} delay={2.3} />
            <Firefly x={520} y={110} delay={1.2} />
          </>
        )}
      </svg>

      <div className="gh-overlay">
        <div className="gh-label">Tài sản trong vườn · {goals.length} cây đang trồng</div>
        <div className="gh-total">{formatVND(total)}</div>
        {weather && (
          <span className={"gh-chip" + (weather.wet ? " wet" : "")}>
            {night ? "🌙" : weather.icon} {night ? weather.text.replace("Trời quang", "Đêm yên ả") : weather.text}
          </span>
        )}
      </div>

      {shown.length === 0 && (
        <div className="gh-empty">
          <span className="gh-chip">🌱 Khu vườn còn trống — gieo hạt giống đầu tiên nào!</span>
          <Link to="/budgets" className="btn primary auto" style={{ width: "auto" }}>+ Gieo hạt (tạo mục tiêu)</Link>
        </div>
      )}
    </div>
  );
}
