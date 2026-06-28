import { useOutletContext } from "react-router-dom";
import MonthPicker from "../components/MonthPicker";
import TransactionSection from "../components/TransactionSection";

// Trang Giao dịch: danh sách + thêm/sửa/xóa + lọc + export theo kỳ.
export default function TransactionsPage() {
  const { month, year, changePeriod, categories, reloadToken, bump } = useOutletContext();
  return (
    <>
      <MonthPicker month={month} year={year} onChange={changePeriod} />
      <TransactionSection month={month} year={year} categories={categories}
                          reloadToken={reloadToken} onChanged={bump} />
    </>
  );
}
