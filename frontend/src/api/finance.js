import client from "./client";

// Gom tất cả lời gọi API tài chính vào 1 chỗ cho gọn.

// ----- Categories -----
export const getCategories = () =>
  client.get("/api/categories").then((r) => r.data);
export const createCategory = (body) =>
  client.post("/api/categories", body).then((r) => r.data);
export const deleteCategory = (id) =>
  client.delete(`/api/categories/${id}`);

// ----- Transactions -----
export const getTransactions = (params) =>
  client.get("/api/transactions", { params }).then((r) => r.data);
export const createTransaction = (body) =>
  client.post("/api/transactions", body).then((r) => r.data);
export const deleteTransaction = (id) =>
  client.delete(`/api/transactions/${id}`);
// Tải file CSV (responseType blob để nhận dữ liệu nhị phân của file).
export const exportTransactions = (params) =>
  client.get("/api/transactions/export", { params, responseType: "blob" });

// ----- Reports -----
export const getMonthlyReport = (month, year) =>
  client.get("/api/reports/monthly", { params: { month, year } }).then((r) => r.data);
export const getYearlyReport = (year) =>
  client.get("/api/reports/yearly", { params: { year } }).then((r) => r.data);
export const getInsights = (month, year) =>
  client.get("/api/reports/insights", { params: { month, year } }).then((r) => r.data);

// ----- Recurring transactions (giao dịch định kỳ) -----
export const getRecurring = () =>
  client.get("/api/recurring").then((r) => r.data);
export const createRecurring = (body) =>
  client.post("/api/recurring", body).then((r) => r.data);
export const updateRecurring = (id, body) =>
  client.put(`/api/recurring/${id}`, body).then((r) => r.data);
export const deleteRecurring = (id) =>
  client.delete(`/api/recurring/${id}`);
export const runRecurring = (id) =>
  client.post(`/api/recurring/${id}/run`).then((r) => r.data);

// ----- Savings goals (mục tiêu tiết kiệm) -----
export const getGoals = () =>
  client.get("/api/goals").then((r) => r.data);
export const createGoal = (body) =>
  client.post("/api/goals", body).then((r) => r.data);
export const deleteGoal = (id) =>
  client.delete(`/api/goals/${id}`);
export const contributeGoal = (id, amount) =>
  client.post(`/api/goals/${id}/contribute`, { amount }).then((r) => r.data);

// ----- Budgets -----
export const getBudgetStatus = (month, year) =>
  client.get("/api/budgets/status", { params: { month, year } }).then((r) => r.data);
export const setBudget = (body) =>
  client.post("/api/budgets", body).then((r) => r.data);
