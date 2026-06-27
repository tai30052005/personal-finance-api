import { useState } from "react";
import { uploadReceipt, cloudinaryConfigured } from "../api/cloudinary";

// Ô đính ảnh hóa đơn: chọn file -> upload lên Cloudinary -> trả URL qua onChange.
// value = URL ảnh hiện tại ("" nếu chưa có).
export default function ReceiptUpload({ value, onChange }) {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");

  async function handleFile(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setError("");
    setUploading(true);
    try {
      const url = await uploadReceipt(file);
      onChange(url);
    } catch (err) {
      setError(err.message || "Upload thất bại");
    } finally {
      setUploading(false);
      e.target.value = ""; // reset để có thể chọn lại cùng một file
    }
  }

  // Chưa cấu hình Cloudinary -> ẩn hẳn để không gây nhầm lẫn khi demo.
  if (!cloudinaryConfigured) return null;

  return (
    <div className="receipt-upload">
      {value ? (
        <div className="receipt-preview">
          <a href={value} target="_blank" rel="noreferrer" title="Xem ảnh gốc">
            <img src={value} alt="Hóa đơn" />
          </a>
          <button type="button" className="btn ghost sm auto" onClick={() => onChange("")}>
            Bỏ ảnh
          </button>
        </div>
      ) : (
        <label className="btn auto sm">
          {uploading ? "Đang tải ảnh..." : "📎 Đính ảnh hóa đơn"}
          <input type="file" accept="image/*" hidden disabled={uploading} onChange={handleFile} />
        </label>
      )}
      {error && <div className="alert error">{error}</div>}
    </div>
  );
}
