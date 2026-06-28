import { useOutletContext } from "react-router-dom";
import MonthPicker from "../components/MonthPicker";
import BudgetSection from "../components/BudgetSection";
import GoalsSection from "../components/GoalsSection";

// Trang Ngân sách & Mục tiêu: hạn mức chi theo tháng + mục tiêu tiết kiệm.
export default function BudgetsGoalsPage() {
  const { month, year, changePeriod, categories, reloadToken, bump } = useOutletContext();
  return (
    <>
      <MonthPicker month={month} year={year} onChange={changePeriod} />
      <BudgetSection month={month} year={year} categories={categories}
                     reloadToken={reloadToken} onChanged={bump} />
      <GoalsSection reloadToken={reloadToken} onChanged={bump} />
    </>
  );
}
