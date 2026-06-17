# ============================================================
#  Multi-stage build: tách "build" và "run" thành 2 giai đoạn
#  -> image cuối nhỏ gọn (chỉ chứa JRE + file .jar, không kèm Maven/source)
# ============================================================

# ---------- STAGE 1: BUILD ----------
# Dùng image có sẵn Maven + JDK 17 để biên dịch & đóng gói .jar
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml trước và tải dependency riêng một bước.
# Mẹo: Docker cache theo từng lệnh -> nếu pom.xml không đổi,
# lần build sau dùng lại lớp dependency đã tải (build nhanh hơn nhiều).
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Giờ mới copy mã nguồn và build ra file .jar (bỏ qua test để build nhanh)
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---------- STAGE 2: RUNTIME ----------
# Chỉ cần JRE (không cần JDK/Maven) -> image nhẹ hơn
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Bảo mật: tạo user thường, không chạy app bằng root
RUN useradd --system --no-create-home spring
USER spring

# Lấy file .jar đã build từ stage 1 sang, đặt tên cố định là app.jar
COPY --from=build /app/target/*.jar app.jar

# App lắng nghe cổng 8080 (chỉ mang tính tài liệu, không tự mở port)
EXPOSE 8080

# Lệnh chạy khi container khởi động
ENTRYPOINT ["java", "-jar", "app.jar"]
