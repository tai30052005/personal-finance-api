import { useOutletContext } from "react-router-dom";
import { useConcept } from "../theme/ConceptContext";
import MonthPicker from "../components/MonthPicker";
import GardenHero from "../components/GardenHero";
import BalanceCard from "../components/BalanceCard";
import SummarySection from "../components/SummarySection";
import InsightsSection from "../components/InsightsSection";
import ChartsSection from "../components/ChartsSection";

// Trang Tổng quan: tóm tắt + phân tích + biểu đồ theo kỳ.
// Concept "Vườn Xanh" có thêm hàng hero: khu vườn mục tiêu + thẻ số dư tối
// (khi đó SummarySection chuyển sang dạng gọn để khỏi trùng số liệu).
export default function OverviewPage() {
  const { month, year, changePeriod, reloadToken } = useOutletContext();
  const { concept } = useConcept();
  const garden = concept === "garden";

  return (
    <>
      <MonthPicker month={month} year={year} onChange={changePeriod} />
      {garden && (
        <div className="hero-row">
          <GardenHero month={month} year={year} reloadToken={reloadToken} />
          <BalanceCard month={month} year={year} reloadToken={reloadToken} />
        </div>
      )}
      <SummarySection month={month} year={year} reloadToken={reloadToken} compact={garden} />
      <InsightsSection month={month} year={year} reloadToken={reloadToken} />
      <ChartsSection month={month} year={year} reloadToken={reloadToken} />
    </>
  );
}
