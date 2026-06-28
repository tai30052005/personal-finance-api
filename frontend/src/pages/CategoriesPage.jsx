import { useOutletContext } from "react-router-dom";
import CategorySection from "../components/CategorySection";

// Trang Danh mục: thêm/xóa danh mục thu–chi.
export default function CategoriesPage() {
  const { categories, bump } = useOutletContext();
  return <CategorySection categories={categories} onChanged={bump} />;
}
