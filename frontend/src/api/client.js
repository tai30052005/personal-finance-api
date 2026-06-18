import axios from "axios";

// Tạo sẵn một "axios instance" với địa chỉ gốc của backend.
// - Dev: VITE_API_URL = http://localhost:8080 (gọi trực tiếp backend, có CORS).
// - Docker (production build): VITE_API_URL rỗng -> gọi đường dẫn tương đối "/api/...",
//   để Nginx proxy sang backend (cùng origin -> không cần CORS).
const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "",
});

// Interceptor REQUEST: tự đính token vào header Authorization trước mỗi request.
// Nhờ vậy ta không phải tự thêm token thủ công ở từng lời gọi.
client.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor RESPONSE: nếu server trả 401 (token sai/hết hạn) -> xóa token,
// đẩy người dùng về trang đăng nhập.
client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const path = window.location.pathname;
    if (status === 401 && path !== "/login" && path !== "/register") {
      localStorage.removeItem("token");
      localStorage.removeItem("email");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default client;
