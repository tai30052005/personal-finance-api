import { Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardLayout from "./pages/DashboardLayout";
import OverviewPage from "./pages/OverviewPage";
import ChatPage from "./pages/ChatPage";
import TransactionsPage from "./pages/TransactionsPage";
import BudgetsGoalsPage from "./pages/BudgetsGoalsPage";
import RecurringPage from "./pages/RecurringPage";
import CategoriesPage from "./pages/CategoriesPage";
import SettingsPage from "./pages/SettingsPage";
import ProtectedRoute from "./components/ProtectedRoute";

// Khai báo các "đường" (route) của app.
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Khu vực cần đăng nhập: dùng chung khung DashboardLayout (sidebar + topbar),
          mỗi mục là một trang con render vào <Outlet/>. */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<OverviewPage />} />
        <Route path="assistant" element={<ChatPage />} />
        <Route path="transactions" element={<TransactionsPage />} />
        <Route path="budgets" element={<BudgetsGoalsPage />} />
        <Route path="recurring" element={<RecurringPage />} />
        <Route path="categories" element={<CategoriesPage />} />
        <Route path="settings" element={<SettingsPage />} />
      </Route>

      {/* Đường lạ -> về trang chủ */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
