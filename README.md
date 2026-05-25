# 🔨 Hệ Thống Đấu Giá Trực Tuyến - Online Auction System (Group 7 - CS6)

Chào mừng bạn đến với **Hệ thống Đấu giá Trực tuyến (Online Auction System)**, một dự án được phát triển bởi các sinh viên năm nhất ngành Khoa học Máy tính thuộc trường Đại học Công nghệ (UET) trong khuôn khổ môn học Lập trình nâng cao. 

Dự án này là một hệ thống ứng dụng Client-Server hoàn chỉnh, tích hợp giao diện người dùng JavaFX hiện đại, truyền thông Socket thời gian thực và quản lý cơ sở dữ liệu đám mây bảo mật cao.

---

## 📌 1. Mô tả bài toán và Phạm vi hệ thống

### 🔹 Bài toán đặt ra
Trong thương mại điện tử, việc mua bán thông thường đôi khi không phản ánh đúng giá trị thực tế của các mặt hàng đặc biệt, đồ sưu tầm hoặc các sản phẩm có độ khan hiếm cao. Hệ thống đấu giá trực tuyến ra đời nhằm giải quyết vấn đề này bằng cách tạo ra một môi trường cạnh tranh công bằng, minh bạch giữa những người mua (Bidders), đồng thời tối ưu hóa lợi nhuận cho người bán (Sellers).

### 🔹 Phạm vi hệ thống
Hệ thống hỗ trợ 3 vai trò (roles) chính với các quyền hạn và luồng nghiệp vụ khép kín:
1. **Người mua (Bidder)**: Tìm kiếm sản phẩm, đặt giá thời gian thực, quản lý ví điện tử cá nhân (nạp/rút tiền), nhận thông báo và thanh toán đơn hàng thắng cuộc.
2. **Người bán (Seller)**: Đăng tải sản phẩm mới đấu giá với các thuộc tính linh hoạt, theo dõi diễn biến đặt giá và quản lý quy trình giao dịch, xuất hóa đơn PDF chuyên nghiệp khi hoàn thành.
3. **Quản trị viên (Admin)**: Duyệt yêu cầu nâng cấp người bán, phê duyệt các giao dịch nạp/rút tiền, kiểm duyệt sản phẩm đấu giá và quản lý tài khoản người dùng để đảm bảo tính an toàn cho hệ thống.

---

## 💻 2. Công nghệ sử dụng, Môi trường chạy & Yêu cầu cài đặt

### ⚙️ Công nghệ sử dụng
* **Ngôn ngữ lập trình**: Java 21 (JDK 21)
* **Giao diện người dùng (GUI)**: JavaFX 21.0.6 (Hỗ trợ cấu trúc Module - JPMS)
* **Truyền thông mạng (Networking)**: TCP Socket thời gian thực (Custom Request/Response Protocol)
* **Cơ sở dữ liệu (Database)**: TiDB (Cloud-hosted MySQL-compatible database trên Alibaba Cloud)
* **Quản lý kết nối (Connection Pool)**: HikariCP (Đảm bảo hiệu năng kết nối DB cao, tự động khôi phục kết nối và duy trì keep-alive)
* **Ghi nhật ký (Logging)**: SLF4J + Logback
* **Công cụ xây dựng**: Maven
* **Thư viện bên thứ ba**:
  * **Lombok**: Giảm thiểu mã nguồn boilerplate (Getter, Setter, Constructor, Builder).
  * **jBCrypt**: Mã hóa băm mật khẩu một chiều bảo mật cao.
  * **Gson**: Tuần tự hóa và giải tuần tự hóa dữ liệu JSON để giao tiếp qua Socket.
  * **OpenHTMLtoPDF**: Kết xuất hóa đơn thanh toán từ mẫu HTML sang tệp PDF chuyên nghiệp.
  * **ControlsFX & Ikonli (FontAwesome 5)**: Hỗ trợ các biểu tượng (icons) hiện đại và bộ điều khiển giao diện nâng cao cho JavaFX.

### 🌐 Môi trường chạy & Yêu cầu cài đặt
1. **Java Development Kit**: JDK 21 hoặc mới hơn đã được cài đặt và cấu hình biến môi trường (`JAVA_HOME`).
2. **Maven**: Đã cài đặt cục bộ (hoặc sử dụng trực tiếp Maven Wrapper `./mvnw` đính kèm trong thư mục gốc).
3. **Mạng internet**: Bắt buộc phải có kết nối Internet để kết nối tới Cloud Database (TiDB).
4. **Cổng dịch vụ (Ports)**: Đảm bảo các cổng sau không bị chiếm dụng trên máy chạy Server:
   * **`8080`**: Cổng kết nối TCP Socket chính cho dịch vụ Đấu giá.
   * **`9090`**: Cổng HTTP Static Server dùng để lưu trữ và tải ảnh sản phẩm.

---

## 📂 3. Cấu trúc thư mục và các Module chính

Dự án được cấu trúc theo mô hình **Multi-module Maven** giúp quản lý mã nguồn rõ ràng và tái sử dụng tối đa các lớp dùng chung:

```
Auction_system (Thư mục gốc)
├── config.properties                 # Cấu hình IP và Port của Server
├── pom.xml                           # Maven parent cấu hình dependency chung
├── uploads/                          # Thư mục lưu trữ hình ảnh sản phẩm được tải lên
├── Auction_shared/                   # MODULE DÙNG CHUNG (Shared Module)
│   ├── pom.xml
│   └── src/main/java
│       └── com/auction/shared
│           ├── enums/                # Định nghĩa các trạng thái hệ thống (AuctionStatus, OrderStatus...)
│           ├── model/                # Thực thể lõi (User, Auction, Order, Notification, Transaction...)
│           ├── network/              # Lớp cấu hình mạng (NetworkConfig) đọc từ config.properties
│           ├── request/              # Định nghĩa các mẫu yêu cầu gửi từ Client
│           ├── response/             # Định nghĩa các mẫu phản hồi gửi từ Server
│           └── util/                 # Các lớp tiện ích chung
├── Auction_server/                   # MODULE BACKEND (Server Module)
│   ├── pom.xml
│   └── src/main/java
│       ├── config/                   # Cấu hình DatabaseConnection sử dụng HikariCP kết nối TiDB
│       ├── repository/               # Các lớp tương tác trực tiếp với Database (UserRepo, AuctionRepo...)
│       ├── scheduler/                # Các tiến trình ngầm (đóng đấu giá tự động, hủy đơn quá hạn 7 ngày)
│       ├── servercontroller/         # Quản lý TCP Socket Server (Server, ClientHandler, AuctionRooms)
│       └── service/                  # Xử lý nghiệp vụ chính & ImageHttpServer lưu trữ ảnh tĩnh
└── Auction_client/                   # MODULE FRONTEND (Client Module)
    ├── pom.xml
    └── src/main/resources/           # Chứa các file giao diện .fxml, tệp định dạng .css và font chữ
    └── src/main/java
        └── com/auction/client
            ├── Main.java             # Điểm khởi chạy của ứng dụng Client JavaFX
            ├── network/              # Quản lý kết nối Socket đến Server (ServerConnection)
            ├── service/              # Xử lý các logic nghiệp vụ giao tiếp tại Client
            └── screenhandler/        # Các Controller quản lý sự kiện của màn hình GUI
                └── admin/            # Các Controller dành riêng cho phân hệ Admin
```

---

## 📦 4. Vị trí các tệp `.jar` sau khi đóng gói

Sau khi build dự án thành công bằng lệnh Maven, các tệp đóng gói sẽ nằm tại thư mục `target` của mỗi module:

* **Tệp dùng chung (Library)**:
  `Auction_shared/target/Auction_shared-1.0-SNAPSHOT.jar`
* **Ứng dụng Client (JavaFX Application)**:
  `Auction_client/target/Auction_client-1.0-SNAPSHOT.jar`
* **Ứng dụng Server (Standard JAR)**:
  `Auction_server/target/Auction_server-0.0.1-SNAPSHOT.jar`
* **Ứng dụng Server tự chạy (Executable Fat JAR - Có sẵn tất cả thư viện đi kèm)**:
  `Auction_server/target/Auction_server-0.0.1-SNAPSHOT-jar-with-dependencies.jar`

---

## 🚀 5. Hướng dẫn khởi chạy chi tiết (Server & Client)

Vui lòng tuân thủ đúng thứ tự khởi chạy dưới đây để tránh các lỗi ngắt kết nối socket:

### 🛠️ Bước 1: Biên dịch và đóng gói toàn bộ dự án
Mở cửa sổ dòng lệnh (Terminal/Command Prompt) tại thư mục gốc của dự án và chạy lệnh sau để Maven tải các dependency và đóng gói:
```bash
# Sử dụng Maven Wrapper tích hợp sẵn trong dự án:
.\mvnw clean package -DskipTests
```
*(Lệnh này sẽ tự động dọn dẹp các thư mục build cũ, biên dịch mã nguồn Java 21, xử lý annotation của Lombok, và sinh ra các tệp `.jar` tại thư mục target tương ứng).*

### 🖥️ Bước 2: Khởi chạy Server trước (BẮT BUỘC)
Server phải được bật trước để lắng nghe kết nối socket từ phía Client. Bạn có hai cách chạy:

* **Cách 1: Chạy trực tiếp qua tệp thi hành Fat JAR (Khuyên dùng)**:
  ```bash
  java -jar Auction_server/target/Auction_server-0.0.1-SNAPSHOT-jar-with-dependencies.jar
  ```
* **Cách 2: Chạy thông qua Maven Command**:
  ```bash
  .\mvnw -pl Auction_server compile exec:java -Dexec.mainClass="servercontroller.Server"
  ```
> **💡 Ghi chú**: Khi Server khởi động thành công, nó sẽ tự động chạy hai dịch vụ song song:
> 1. Dịch vụ TCP Socket chính kết nối cổng `8080`.
> 2. Dịch vụ HTTP Image Server lưu trữ và phân phát ảnh sản phẩm ở cổng `9090`.

### 👥 Bước 3: Khởi chạy ứng dụng Client
Bạn có hai cách để chạy giao diện người dùng JavaFX:

* **Cách 1: Chạy thông qua Maven Command**:
  ```bash
  .\mvnw -pl Auction_client compile exec:java -Dexec.mainClass="com.auction.client.Main"
  ```
* **Cách 2: Chạy trực tiếp trên các IDE hiện đại**:
  * Mở thư mục dự án gốc bằng **IntelliJ IDEA** (khuyên dùng) hoặc **Eclipse**.
  * Chờ IDE đồng bộ và tải cấu hình Maven.
  * Tìm tới lớp `com.auction.client.Main` tại module `Auction_client`.
  * Nhấn nút **Run** (hoặc tổ hợp phím chạy nhanh) để hiển thị giao diện đăng nhập ứng dụng.

---

## ✅ 6. Danh sách các chức năng đã hoàn thành

Hệ thống đã được thiết kế và triển khai hoàn thiện 100% các chức năng nghiệp vụ đấu giá chuyên nghiệp:

### 🔑 6.1. Xác thực & Quản lý Tài khoản (Authentication & Accounts)
- [x] **Đăng ký (Sign Up)**: Đăng ký tài khoản Bidder mới, kiểm tra trùng lặp email/username, định dạng trường thông tin.
- [x] **Đăng ký làm Người bán (Seller Register)**: Người dùng có thể điền thông tin và gửi yêu cầu nâng cấp tài khoản lên Seller (được Admin phê duyệt).
- [x] **Đăng nhập bảo mật (Login)**: Xác thực đăng nhập qua mật khẩu băm BCrypt, cho phép đăng nhập nhanh bằng phím `Enter`.
- [x] **Cơ chế Single Session**: Tự động phát hiện và ngăn chặn việc một tài khoản đăng nhập đồng thời trên nhiều thiết bị khác nhau. Giải quyết triệt để lỗi treo phiên đăng nhập khi Client đóng ứng dụng đột ngột.
- [x] **Hồ sơ cá nhân (Profile & Edit Profile)**: Xem và cập nhật các thông tin cá nhân bao gồm họ tên, số điện thoại, mật khẩu, và ảnh đại diện.

### 💰 6.2. Phân hệ Ví điện tử & Giao dịch (E-Wallet & Transactions)
- [x] **Ví điện tử tích hợp**: Xem chi tiết số dư khả dụng (Available Balance) và số dư bị tạm đóng băng (Frozen Balance).
- [x] **Cơ chế Đóng băng số dư (Balance Freeze)**: Khi Bidder đặt giá cao nhất trong một phiên, hệ thống sẽ tạm thời đóng băng số tiền tương ứng để đảm bảo tính an toàn thanh toán. Khi bị người khác trả giá cao hơn, số tiền này sẽ tự động được hoàn trả về số dư khả dụng ngay lập tức.
- [x] **Nạp tiền (Deposit) & Rút tiền (Withdraw)**: Lập các yêu cầu giao dịch nạp/rút tiền tiện lợi và đưa vào danh sách chờ quản trị viên phê duyệt.

### 🔨 6.3. Phân hệ Đấu giá cho Người mua (Bidder Features)
- [x] **Màn hình trang chủ hiện đại (Homepage)**: Hiển thị các sản phẩm đang diễn ra đấu giá trực quan với thanh tìm kiếm và phân loại sản phẩm.
- [x] **Phòng đấu giá thời gian thực (Auction Room)**:
  - Đồng hồ đếm ngược (Countdown Timer) trực quan hiển thị số ngày, giờ, phút, giây còn lại của phiên.
  - Lịch sử đặt giá cập nhật theo thời gian thực (giây) cho mọi người dùng trong phòng đấu giá qua cơ chế Broadcast Socket.
- [x] **Đặt giá (Bidding)**: Đưa ra mức giá mong muốn (phải lớn hơn giá cao nhất hiện tại cộng với bước giá tối thiểu và số dư khả dụng của ví phải đủ).
- [x] **Tự động gia hạn (Auto-Extend)**: Nếu có người đặt giá mới trong vòng 30 giây cuối cùng trước khi hết giờ, phiên đấu giá sẽ tự động kéo dài thêm 30 giây để tạo môi trường cạnh tranh lành mạnh.

### 📦 6.4. Phân hệ Quản lý cho Người bán (Seller Features)
- [x] **Đăng tải đấu giá mới (Upload Item)**: Người bán đăng sản phẩm kèm theo tên, giá khởi điểm, bước giá tối thiểu, thời gian kết thúc, ảnh mô tả trực quan và các thông số thuộc tính động (Dynamic Attributes).
- [x] **Xem chi tiết sản phẩm (Item View)**: Người bán xem lại thông tin chi tiết và lịch sử các lượt đấu giá của sản phẩm mình đã đăng.
- [x] **Quản lý sản phẩm**: Màn hình xem các sản phẩm đang chờ duyệt, đang diễn ra đấu giá và đã hoàn thành.

### 🧾 6.5. Quản lý Đơn hàng & Xuất hóa đơn (Orders & PDF Invoices)
- [x] **Xác nhận giao dịch tự động**: Khi phiên đấu giá kết thúc có người thắng cuộc, hệ thống tự động khởi tạo đơn hàng ở trạng thái *Chờ xác nhận (Pending)*.
- [x] **Hóa đơn và Thanh toán**: 
  - Người thắng cuộc thực hiện thanh toán trực tiếp từ số dư ví điện tử của mình. 
  - Người bán và Người mua có thể xác nhận đơn hàng đã giao dịch thành công.
- [x] **Xuất hóa đơn PDF chuyên nghiệp**: Tích hợp công cụ chuyển đổi HTML Template sang định dạng tệp tin `.pdf` sắc nét, cho phép tải xuống và lưu giữ hóa đơn giao dịch chính thức.

### 🛡️ 6.6. Bảng điều khiển Quản trị viên (Admin Dashboard)
- [x] **Quản lý người dùng (User Manager)**: Danh sách toàn bộ thành viên, cho phép khóa/mở khóa tài khoản vi phạm.
- [x] **Phê duyệt người bán (Seller Account Manager)**: Phê duyệt/Từ chối các yêu cầu nâng cấp lên tài khoản Người bán.
- [x] **Duyệt giao dịch tài chính (Pending Transaction Manager)**: Phê duyệt/Từ chối các giao dịch nạp tiền hoặc rút tiền từ ví của thành viên.
- [x] **Quản lý phiên đấu giá (Auction Manager)**: Phê duyệt các sản phẩm mới đăng để chính thức bắt đầu phiên đấu giá công khai.

### 🕒 6.7. Tiến trình Hệ thống tự động (Automated Schedulers)
- [x] **Auction Status Scheduler**: Tiến trình chạy ngầm ở Server tự động kiểm tra mỗi giây để quét các phiên đấu giá đã chạm mốc thời gian kết thúc, xác định người chiến thắng cao nhất và khởi tạo đơn hàng tương ứng, hoặc hủy phiên đấu giá nếu không có ai đặt giá.
- [x] **Order Expiry Scheduler**: Tiến trình ngầm tự động hủy đơn hàng quá hạn thanh toán 7 ngày (Pending status) và hoàn lại tiền cọc/áp dụng các biện pháp kỷ luật tài khoản theo quy định.

---
*Chúc các bạn có những trải nghiệm đấu giá tuyệt vời cùng **Online Auction System**! Mọi phản hồi hoặc báo cáo lỗi vui lòng tạo Issue trên kho lưu trữ của nhóm.*
