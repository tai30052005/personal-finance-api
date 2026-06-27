-- V4: Thêm ảnh hóa đơn cho giao dịch.
-- Lưu URL ảnh (do Cloudinary trả về sau khi upload), tùy chọn.
ALTER TABLE transactions ADD COLUMN receipt_url VARCHAR(500);
