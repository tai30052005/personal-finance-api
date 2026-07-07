import { createContext, useContext, useEffect, useState } from "react";

/**
 * Hệ thống "concept" (bộ giao diện) + chế độ sáng/tối cho toàn app.
 *
 * Hai trục ĐỘC LẬP, phản chiếu lên <html> để CSS variables đổi theo:
 *   - data-concept: "classic" | "garden" | ... (bộ da)
 *   - data-theme  : "" | "dark"               (sáng / tối)
 *
 * Thêm concept mới = thêm 1 entry vào CONCEPTS + 1 khối CSS
 * [data-concept="..."] (và biến thể [data-theme="dark"]) trong index.css.
 */
export const CONCEPTS = [
  { id: "classic", name: "Cổ điển", description: "Navy chuyên nghiệp, gọn gàng, trung tính." },
  { id: "garden", name: "Vườn Xanh", description: "Khu vườn tài chính — tiết kiệm là chăm cây, tiền bạc nở hoa." },
];

const ConceptContext = createContext(null);

export function ConceptProvider({ children }) {
  const [concept, setConcept] = useState(() => localStorage.getItem("concept") || "garden");
  const [dark, setDark] = useState(() => localStorage.getItem("theme") === "dark");

  useEffect(() => {
    document.documentElement.dataset.concept = concept;
    localStorage.setItem("concept", concept);
  }, [concept]);

  useEffect(() => {
    document.documentElement.dataset.theme = dark ? "dark" : "";
    localStorage.setItem("theme", dark ? "dark" : "light");
  }, [dark]);

  const value = { concept, setConcept, dark, toggleDark: () => setDark((d) => !d) };
  return <ConceptContext.Provider value={value}>{children}</ConceptContext.Provider>;
}

export function useConcept() {
  return useContext(ConceptContext);
}
