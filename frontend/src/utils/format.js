// Định dạng tiền kiểu Việt Nam, vd 1500000 -> "1.500.000 ₫"
export function formatVND(value) {
  const n = Number(value) || 0;
  return n.toLocaleString("vi-VN") + " ₫";
}
