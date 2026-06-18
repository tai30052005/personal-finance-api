import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

// Bọc quanh các trang cần đăng nhập. Nếu chưa đăng nhập -> chuyển hướng về /login.
export default function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/login" replace />;
}
