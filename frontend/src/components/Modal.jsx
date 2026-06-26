import { useEffect } from "react";

// Modal đơn giản, tái sử dụng: lớp phủ mờ + hộp nội dung ở giữa.
// Đóng khi bấm nền, bấm nút ✕, hoặc nhấn phím Esc.
export default function Modal({ title, onClose, children }) {
  useEffect(() => {
    function onKey(e) {
      if (e.key === "Escape") onClose();
    }
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  return (
    <div className="modal-overlay" onClick={onClose}>
      {/* Chặn nổi bọt để bấm bên trong hộp không làm đóng modal. */}
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-head">
          <h3>{title}</h3>
          <button className="chip-x" onClick={onClose} aria-label="Đóng">✕</button>
        </div>
        {children}
      </div>
    </div>
  );
}
