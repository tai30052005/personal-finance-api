import { useOutletContext } from "react-router-dom";
import MonthPicker from "../components/MonthPicker";
import SummarySection from "../components/SummarySection";
import InsightsSection from "../components/InsightsSection";
import ChartsSection from "../components/ChartsSection";

// Trang Tổng quan: tóm tắt + phân tích + biểu đồ theo kỳ.
export default function OverviewPage() {
  const { month, year, changePeriod, reloadToken } = useOutletContext();
  return (
    <>
      <MonthPicker month={month} year={year} onChange={changePeriod} />
      <SummarySection month={month} year={year} reloadToken={reloadToken} />
      <InsightsSection month={month} year={year} reloadToken={reloadToken} />
      <ChartsSection month={month} year={year} reloadToken={reloadToken} />
    </>
  );
}
