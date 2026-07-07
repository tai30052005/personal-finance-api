import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { AuthProvider } from "./auth/AuthContext";
import { ConceptProvider } from "./theme/ConceptContext";
import ErrorBoundary from "./components/ErrorBoundary";
import App from "./App.jsx";
import "./index.css";

// Áp concept + theme đã lưu TRƯỚC khi render để không bị "nháy" giao diện.
document.documentElement.dataset.concept = localStorage.getItem("concept") || "garden";
if (localStorage.getItem("theme") === "dark") {
  document.documentElement.dataset.theme = "dark";
}

// Điểm khởi động React. Bọc app trong:
//  - BrowserRouter: bật định tuyến theo URL
//  - AuthProvider : cung cấp thông tin đăng nhập cho toàn app
createRoot(document.getElementById("root")).render(
  <StrictMode>
    <BrowserRouter>
      <ErrorBoundary>
        <ConceptProvider>
          <AuthProvider>
            <App />
          </AuthProvider>
        </ConceptProvider>
      </ErrorBoundary>
    </BrowserRouter>
  </StrictMode>
);
