// Định dạng tiền kiểu Việt Nam, vd 1500000 -> "1.500.000 ₫"
export function formatVND(value) {
  const n = Number(value) || 0;
  return n.toLocaleString("vi-VN") + " ₫";
}

// Sinh màu ổn định từ tên danh mục (hash tên -> hue) để chấm màu
// của cùng một danh mục luôn giống nhau, không cần lưu màu vào DB.
export function categoryColor(name) {
  let h = 0;
  for (const ch of String(name || "")) {
    h = (h * 31 + ch.codePointAt(0)) % 360;
  }
  return `hsl(${h} 65% 48%)`;
}
