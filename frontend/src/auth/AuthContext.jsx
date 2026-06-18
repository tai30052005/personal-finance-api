import { createContext, useContext, useState } from "react";
import client from "../api/client";

// Context = "kho dữ liệu dùng chung" cho toàn app (ở đây: thông tin đăng nhập).
// Nhờ Context, mọi trang đều biết user đã đăng nhập chưa mà không phải truyền props lằng nhằng.
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // Đọc token/email đã lưu trong localStorage (giữ đăng nhập sau khi F5).
  const [token, setToken] = useState(() => localStorage.getItem("token"));
  const [email, setEmail] = useState(() => localStorage.getItem("email"));

  function saveAuth(data) {
    localStorage.setItem("token", data.token);
    localStorage.setItem("email", data.email);
    setToken(data.token);
    setEmail(data.email);
  }

  async function login(email, password) {
    const { data } = await client.post("/api/auth/login", { email, password });
    saveAuth(data);
  }

  async function register(email, password) {
    const { data } = await client.post("/api/auth/register", { email, password });
    saveAuth(data);
  }

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    setToken(null);
    setEmail(null);
  }

  const value = { token, email, login, register, logout, isAuthenticated: !!token };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// Hook tiện ích: gọi useAuth() ở bất kỳ component nào để lấy thông tin/đăng nhập.
export function useAuth() {
  return useContext(AuthContext);
}
