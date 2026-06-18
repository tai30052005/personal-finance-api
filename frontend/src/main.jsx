import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { AuthProvider } from "./auth/AuthContext";
import App from "./App.jsx";
import "./index.css";

// Điểm khởi động React. Bọc app trong:
//  - BrowserRouter: bật định tuyến theo URL
//  - AuthProvider : cung cấp thông tin đăng nhập cho toàn app
createRoot(document.getElementById("root")).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>
);
