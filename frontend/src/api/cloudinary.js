// Upload ảnh trực tiếp lên Cloudinary bằng "unsigned upload preset".
// File đi thẳng từ trình duyệt -> Cloudinary, KHÔNG qua backend của ta.
//
// Cần 2 biến môi trường (đặt ở .env khi dev, hoặc Vercel khi deploy) —
// KHÔNG để secret trong code:
//   VITE_CLOUDINARY_CLOUD_NAME    : tên cloud (vd: "dxxxx")
//   VITE_CLOUDINARY_UPLOAD_PRESET : tên unsigned upload preset đã bật trên Cloudinary
const CLOUD_NAME = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME;
const UPLOAD_PRESET = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET;

// Bật/tắt tính năng tùy theo đã cấu hình hay chưa (ẩn nút nếu chưa có).
export const cloudinaryConfigured = Boolean(CLOUD_NAME && UPLOAD_PRESET);

/** Upload 1 file ảnh, trả về secure_url để lưu vào giao dịch. */
export async function uploadReceipt(file) {
  if (!cloudinaryConfigured) {
    throw new Error("Chưa cấu hình Cloudinary (VITE_CLOUDINARY_CLOUD_NAME / _UPLOAD_PRESET).");
  }
  const form = new FormData();
  form.append("file", file);
  form.append("upload_preset", UPLOAD_PRESET);

  const res = await fetch(
    `https://api.cloudinary.com/v1_1/${CLOUD_NAME}/image/upload`,
    { method: "POST", body: form }
  );
  if (!res.ok) {
    throw new Error("Upload ảnh thất bại");
  }
  const data = await res.json();
  return data.secure_url;
}
