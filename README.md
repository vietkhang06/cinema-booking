# ĐỒ ÁN NGÔN NGỮ LẬP TRÌNH JAVA - SE330.Q21

## Thực hiện bởi Nhóm 8

---

# Giới thiệu

Đây là đồ án môn **Ngôn Ngữ Lập Trình Java - SE330.Q21**, được thực hiện bởi **Nhóm 8**.
Đề tài xây dựng một hệ thống **Cinema Booking App** hỗ trợ đầy đủ các vai trò **Customer**, **Staff** và **Admin**, bao gồm các chức năng đặt vé, quản lý suất chiếu, quản lý rạp, thanh toán, quét mã vé và đồng bộ dữ liệu theo thời gian thực.

Mục tiêu của đồ án là áp dụng kiến thức Java vào một hệ thống thực tế, từ thiết kế kiến trúc phần mềm, xây dựng giao diện Android, đến phát triển backend Spring Boot và tích hợp Firebase.

---

# Giới thiệu thành viên nhóm

| STT | Họ và tên            | Vai trò            | Nhiệm vụ chính                                          |
| --- | ------------         | ------------------ | ------------------------------------------------------- |
| 1   | Đoàn Việt Khang      | Nhóm trưởng / Lead | Phân tích hệ thống, kiến trúc, backend, đồng bộ dữ liệu |
| 2   | Phạm Ngọc Gia Khang  | Developer          | Giao diện Customer / Auth                               |
| 3   | Huỳnh Long Bảo Khanh | Developer          | Giao diện Admin / CRUD                                  |
| 4   | Lương Phúc Khang     | Developer          | Giao diện Staff / QR / hỗ trợ vận hành                  |
| 5   | Huỳnh Gia Khang      | Developer          | CineShop / payment / history / UI                       |

# Tổng quan chức năng chính

## 1. Chức năng dành cho Customer

* Đăng nhập / đăng ký / quên mật khẩu
* Xem danh sách phim và banner
* Xem chi tiết phim
* Chọn rạp, ngày chiếu và suất chiếu
* Chọn ghế và đặt vé
* Thanh toán và theo dõi trạng thái thanh toán
* Xem lịch sử giao dịch và chi tiết vé
* Sử dụng CineShop để mua snack / sản phẩm đi kèm
* Xem yêu thích, đánh giá, thông báo

## 2. Chức năng dành cho Admin

* Quản lý phim
* Quản lý rạp chiếu
* Quản lý phòng chiếu và sơ đồ ghế
* Quản lý suất chiếu
* Quản lý thanh toán
* Xem báo cáo, thống kê và log hệ thống
* Quản lý người dùng

## 3. Chức năng dành cho Staff

* Tra cứu booking
* Quét mã QR vé
* Hỗ trợ khách tại quầy
* Xử lý ghế bị lỗi / giữ ghế / khóa ghế
* Hỗ trợ thanh toán tại quầy
* Xem lịch chiếu, thống kê vận hành
* Ghi nhận lịch sử thao tác và audit log

# Tech Stack

## 1. Android Application

* **Java**: ngôn ngữ chính để phát triển ứng dụng Android
* **Android SDK**: xây dựng giao diện và xử lý logic ứng dụng
* **Material Design**: thiết kế giao diện hiện đại
* **RecyclerView**: hiển thị danh sách dữ liệu động
* **Glide**: tải và hiển thị hình ảnh
* **Retrofit + OkHttp**: gọi API backend
* **Firebase Authentication**: xác thực đăng nhập
* **Firebase Firestore**: lưu trữ và đồng bộ dữ liệu realtime
* **Facebook Login / Google Sign-In**: hỗ trợ đăng nhập mạng xã hội

## 2. Backend

* **Java Spring Boot**: xây dựng REST API và xử lý nghiệp vụ
* **Spring Security**: bảo mật và phân quyền
* **Firebase Admin SDK**: kết nối Firebase từ backend
* **Maven**: quản lý thư viện và build backend

## 3. Công cụ phát triển

* Android Studio
* IntelliJ IDEA
* Git / GitHub
* Firebase Console
* Postman

---

# Java được ứng dụng vào những phần nào

Trong đồ án này, Java được sử dụng ở nhiều tầng khác nhau:

## 1. Tầng giao diện Android

* Xây dựng các Activity, Fragment, Adapter
* Xử lý sự kiện người dùng
* Điều hướng giữa các màn hình
* Kiểm tra dữ liệu nhập và trạng thái giao diện

## 2. Tầng xử lý nghiệp vụ

* Logic đặt vé
* Logic chọn ghế và giữ ghế
* Logic thanh toán
* Logic kiểm tra trạng thái booking / vé
* Logic quét mã QR và xác nhận vé

## 3. Tầng dữ liệu

* DTO, model, mapper
* Repository pattern
* Kết nối Retrofit với backend
* Đồng bộ dữ liệu từ Firestore và API

## 4. Tầng backend

* REST API xử lý phim, rạp, suất chiếu, booking, payment
* Xác thực và phân quyền người dùng
* Xử lý nghiệp vụ seat locking, timeout, realtime sync
* Trả dữ liệu chuẩn cho Android app

## 5. Ứng dụng các kiến thức Java

* OOP: kế thừa, đóng gói, đa hình, trừu tượng
* Collection Framework: List, Map, Set
* Exception Handling: xử lý lỗi hệ thống
* I/O và JSON mapping
* Clean architecture / tách lớp rõ ràng
* Xử lý đồng bộ dữ liệu và trạng thái

---------------
# Tính năng nổi bật

* Phân quyền 3 vai trò: Customer / Staff / Admin
* Đặt vé theo suất chiếu và sơ đồ ghế
* Khóa ghế realtime để tránh tranh chấp
* Thanh toán và theo dõi lịch sử giao dịch
* Staff hỗ trợ quét QR và xử lý tại quầy
* Admin quản lý toàn bộ hệ thống
* CineShop và các tiện ích đi kèm
* Đồng bộ dữ liệu giữa nhiều thiết bị / nhiều vai trò

---

# Hướng dẫn cài đặt và chạy dự án

## 1. Clone repository

```bash
git clone <link-repository>
cd CinemaBooking
```

## 2. Mở ứng dụng Android

* Mở thư mục gốc của project bằng Android Studio
* Sync Gradle
* Cấu hình `google-services.json` trong thư mục `app/`

## 3. Mở backend

* Mở thư mục `cinema-booking-backend/` bằng IntelliJ IDEA hoặc từ terminal
* Chạy backend bằng Maven

```bash
cd cinema-booking-backend
mvn clean spring-boot:run
```

## 4. Cấu hình Firebase

* Thêm Android app vào Firebase project
* Tải `google-services.json`
* Thêm SHA-1 / SHA-256 nếu cần dùng Google Sign-In / Firebase Auth

