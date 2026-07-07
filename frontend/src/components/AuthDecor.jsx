import { useConcept } from "../theme/ConceptContext";

/**
 * Dải trang trí nhỏ cho trang Đăng nhập/Đăng ký của concept Vườn Xanh:
 * đồi cỏ + vài cây con (có bản đêm với trăng + đom đóm khi bật Vườn đêm).
 * Concept Cổ điển không render gì.
 */
export default function AuthDecor() {
  const { concept, dark } = useConcept();
  if (concept !== "garden") return null;

  return (
    <svg viewBox="0 0 340 72" style={{ display: "block", width: "100%", marginTop: 14, borderRadius: 10 }} aria-hidden="true">
      <rect width="340" height="72" fill={dark ? "#0e1a2b" : "#d9edf7"} />
      {dark ? (
        <>
          <circle cx="304" cy="18" r="9" fill="#e8e6d8" />
          <circle cx="300" cy="15" r="2" fill="#cfccba" opacity="0.7" />
          <circle cx="60" cy="14" r="1.2" fill="#fff" opacity="0.9" />
          <circle cx="150" cy="10" r="1" fill="#fff" opacity="0.7" />
          <circle cx="230" cy="18" r="1.2" fill="#fff" opacity="0.8" />
          <circle cx="120" cy="40" r="2" fill="#f6d453">
            <animate attributeName="opacity" values="0.2;1;0.2" dur="2.4s" repeatCount="indefinite" />
          </circle>
          <circle cx="250" cy="34" r="2" fill="#f6d453">
            <animate attributeName="opacity" values="0.2;1;0.2" dur="2.8s" begin="1s" repeatCount="indefinite" />
          </circle>
        </>
      ) : (
        <>
          <circle cx="304" cy="18" r="12" fill="#f2c24e" opacity="0.35" />
          <circle cx="304" cy="18" r="8" fill="#f2c24e" />
          <g fill="#ffffff" opacity="0.9">
            <ellipse cx="110" cy="16" rx="13" ry="5" /><ellipse cx="120" cy="13" rx="8" ry="4" />
          </g>
        </>
      )}
      <path d="M0 52 C60 40 140 50 220 42 C270 37 310 42 340 39 L340 72 L0 72 Z" fill={dark ? "#1a3524" : "#8fc276"} />
      <g transform="translate(70 56)">
        <path d="M0 0 C0 -4 0 -7 0 -10" stroke={dark ? "#2c4a38" : "#3e6b3a"} strokeWidth="2" fill="none" strokeLinecap="round" />
        <path d="M0 -8 C-6 -10 -8 -14 -7 -18 C-2 -16 0 -12 0 -8 Z" fill={dark ? "#528a63" : "#7bb868"} />
        <path d="M0 -8 C6 -10 8 -14 7 -18 C2 -16 0 -12 0 -8 Z" fill={dark ? "#39664a" : "#569148"} />
      </g>
      <g transform="translate(170 52)">
        <path d="M0 0 C-4 -6 -6 -12 -5 -19 C-1 -14 1 -7 0 0 Z" fill={dark ? "#39664a" : "#569148"} />
        <path d="M0 0 C4 -6 6 -12 5 -19 C1 -14 -1 -7 0 0 Z" fill={dark ? "#528a63" : "#7bb868"} />
        <path d="M0 0 C-2 -8 -2 -16 0 -23 C2 -16 2 -8 0 0 Z" fill={dark ? "#447a57" : "#66a863"} />
      </g>
      <g transform="translate(262 50)">
        <path d="M0 0 C0 -8 -1 -15 0 -24" stroke={dark ? "#4a3521" : "#6b4a2e"} strokeWidth="2.6" fill="none" strokeLinecap="round" />
        <circle cx="0" cy="-31" r="10" fill={dark ? "#39664a" : "#569148"} />
        <circle cx="-7" cy="-36" r="7" fill={dark ? "#447a57" : "#66a863"} />
        <circle cx="7" cy="-36" r="6" fill={dark ? "#528a63" : "#7bb868"} />
        <circle cx="-3" cy="-38" r="1.8" fill="#f4a9c4" />
        <circle cx="5" cy="-31" r="1.8" fill="#f4a9c4" />
      </g>
    </svg>
  );
}
