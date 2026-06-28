import { useOutletContext } from "react-router-dom";
import RecurringSection from "../components/RecurringSection";

// Trang Định kỳ: quản lý giao dịch lặp lại hằng tháng.
export default function RecurringPage() {
  const { categories, reloadToken, bump } = useOutletContext();
  return (
    <RecurringSection categories={categories} reloadToken={reloadToken} onChanged={bump} />
  );
}
