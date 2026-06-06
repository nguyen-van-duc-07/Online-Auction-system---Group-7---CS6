# 🔨 Hệ Thống Đấu Giá Trực Tuyến - Online Auction System (Group 7 - CS6)

Chào mừng bạn đến với **Hệ thống Đấu giá Trực tuyến (Online Auction System)**, một dự án được phát triển bởi các sinh viên năm nhất ngành Khoa học Máy tính thuộc trường Đại học Công nghệ (UET) trong khuôn khổ môn học Lập trình nâng cao.

Dự án này là một hệ thống ứng dụng Client-Server hoàn chỉnh, tích hợp giao diện người dùng JavaFX hiện đại, truyền thông Socket thời gian thực và quản lý cơ sở dữ liệu đám mây bảo mật cao.

---

## 📌 1. Mô tả bài toán và Phạm vi hệ thống

### 🔹 Bài toán đặt ra
Trong thương mại điện tử, việc mua bán thông thường đôi khi không phản ánh đúng giá trị thực tế của các mặt hàng đặc biệt, đồ sưu tầm hoặc các sản phẩm có độ khan hiếm cao. Hệ thống đấu giá trực tuyến ra đời nhằm giải quyết vấn đề này bằng cách tạo ra một môi trường cạnh tranh công bằng, minh bạch giữa những người mua (Bidders), đồng thời tối ưu hóa lợi nhuận cho người bán (Sellers).

### 🔹 Phạm vi hệ thống
Hệ thống hỗ trợ 3 vai trò (roles) chính với các quyền hạn và luồng nghiệp vụ khép kín:
1. **Người mua (Bidder)**: Tìm kiếm sản phẩm, đặt giá thời gian thực, cấu hình đấu giá tự động (Auto-Bid), quản lý ví điện tử cá nhân (nạp/rút tiền), nhận thông báo realtime và thanh toán đơn hàng thắng cuộc.
2. **Người bán (Seller)**: Đăng tải sản phẩm mới đấu giá với các thuộc tính linh hoạt (Dynamic Attributes), theo dõi diễn biến đặt giá và quản lý quy trình giao dịch, xuất hóa đơn PDF chuyên nghiệp khi hoàn thành.
3. **Quản trị viên (Admin)**: Duyệt yêu cầu nâng cấp người bán, phê duyệt các giao dịch nạp/rút tiền, kiểm duyệt sản phẩm đấu giá và quản lý tài khoản người dùng để đảm bảo tính an toàn cho hệ thống.

---

## 💻 2. Công nghệ sử dụng, Môi trường chạy & Yêu cầu cài đặt

### ⚙️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| **Ngôn ngữ lập trình** | Java 21 (JDK 21) |
| **Giao diện người dùng (GUI)** | JavaFX 21.0.6 (Hỗ trợ JPMS Module System) |
| **Truyền thông mạng** | TCP Socket thời gian thực (Custom Request/Response Protocol + JSON Serialization) |
| **Cơ sở dữ liệu** | TiDB (Cloud-hosted MySQL-compatible trên Alibaba Cloud) |
| **Connection Pool** | HikariCP (Hiệu năng cao, auto-recovery, keep-alive) |
| **Logging** | SLF4J + Logback |
| **Build Tool** | Apache Maven (Multi-module) |
| **CI/CD** | GitHub Actions (Tự động build & test + Auto Deploy lên Azure VM) |
| **Unit Testing** | JUnit 5 + Mockito + JaCoCo Coverage |

#### 📚 Thư viện bên thứ ba
| Thư viện | Mục đích |
|---|---|
| **Lombok** | Giảm thiểu mã nguồn boilerplate (Getter, Setter, Constructor, Builder) |
| **jBCrypt** | Mã hóa băm mật khẩu một chiều bảo mật cao |
| **Gson** | Tuần tự hóa/giải tuần tự hóa dữ liệu JSON cho giao tiếp Socket |
| **OpenHTMLtoPDF** | Kết xuất hóa đơn thanh toán từ HTML Template sang PDF |
| **ControlsFX & Ikonli (FontAwesome 5)** | Biểu tượng hiện đại và bộ điều khiển giao diện nâng cao cho JavaFX |

### 🌐 Môi trường chạy & Yêu cầu cài đặt
1. **Java Development Kit**: JDK 21 hoặc mới hơn đã được cài đặt và cấu hình biến môi trường (`JAVA_HOME`).
2. **Maven**: Đã cài đặt cục bộ (hoặc sử dụng trực tiếp Maven Wrapper `./mvnw` đính kèm trong thư mục gốc — **không cần cài Maven riêng**).
3. **Mạng internet**: Bắt buộc phải có kết nối Internet để kết nối tới Cloud Database (TiDB).
4. **Cổng dịch vụ (Ports)**: Đảm bảo các cổng sau không bị chiếm dụng trên máy chạy Server:
    * **`8080`**: Cổng kết nối TCP Socket chính cho dịch vụ Đấu giá.
    * **`9090`**: Cổng HTTP Static Server dùng để lưu trữ và tải ảnh sản phẩm.

### 📥 Hướng dẫn cài đặt Java 21 (Dành cho người dùng mới)

Nếu máy bạn **chưa có Java**, hãy làm theo các bước dưới đây:

#### 🪟 Trên Windows

1. **Tải JDK 21** từ trang chính thức Oracle:
   👉 [https://www.oracle.com/java/technologies/downloads/#java21](https://www.oracle.com/java/technologies/downloads/#java21)
   - Chọn tab **Windows** → tải file `.exe` (ví dụ: `jdk-21_windows-x64_bin.exe`).

2. **Chạy trình cài đặt**: Mở file `.exe` vừa tải → nhấn **Next** cho đến khi hoàn tất. Ghi nhớ đường dẫn cài đặt (mặc định: `C:\Program Files\Java\jdk-21`).

3. **Cấu hình biến môi trường `JAVA_HOME`**:
   - Nhấn `Win + S`, gõ **"Biến môi trường"** (hoặc **"Environment Variables"**) → chọn **"Chỉnh sửa biến môi trường hệ thống"**.
   - Nhấn **"Environment Variables..."** → Tại phần **System variables**, nhấn **"New..."**:
     - **Variable name**: `JAVA_HOME`
     - **Variable value**: `C:\Program Files\Java\jdk-21` (đường dẫn cài đặt JDK)
   - Tìm biến **`Path`** trong System variables → nhấn **Edit** → nhấn **New** → thêm: `%JAVA_HOME%\bin`
   - Nhấn **OK** tất cả các cửa sổ.

4. **Kiểm tra cài đặt**: Mở **Command Prompt** mới (nhấn `Win + R`, gõ `cmd`) và chạy:
   ```bash
   java -version
   ```
   Kết quả mong đợi:
   ```
   java version "21.x.x" ...
   ```

#### 🍎 Trên macOS

1. Tải JDK 21 từ [Oracle Downloads](https://www.oracle.com/java/technologies/downloads/#java21) → chọn tab **macOS** → tải file `.dmg`.
2. Mở file `.dmg` và làm theo hướng dẫn cài đặt.
3. Kiểm tra: mở **Terminal** và chạy `java -version`.

#### 🐧 Trên Linux (Ubuntu/Debian)

```bash
# Cài đặt JDK 21 qua apt:
sudo apt update
sudo apt install openjdk-21-jdk -y

# Cấu hình JAVA_HOME (thêm vào cuối file ~/.bashrc):
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# Kiểm tra:
java -version
```

> [!TIP]
> Sau khi cài Java thành công, bạn **không cần cài Maven** vì dự án đã tích hợp sẵn **Maven Wrapper** (`mvnw` / `mvnw.cmd`). Chỉ cần chạy `.\mvnw` (Windows) hoặc `./mvnw` (macOS/Linux) thay cho lệnh `mvn`.

---

## 📂 3. Cấu trúc thư mục và các Module chính

Dự án được cấu trúc theo mô hình **Multi-module Maven** giúp quản lý mã nguồn rõ ràng và tái sử dụng tối đa các lớp dùng chung:

```
Auction_system (Thư mục gốc)
├── config.properties                 # Cấu hình IP và Port của Server
├── pom.xml                           # Maven parent cấu hình dependency chung
├── .github/workflows/                # CI/CD pipeline (GitHub Actions)
├── uploads/                          # Thư mục lưu trữ hình ảnh sản phẩm được tải lên
│
├── Auction_shared/                   # 📦 MODULE DÙNG CHUNG (Shared Module)
│   ├── pom.xml
│   └── src/main/java/com/auction/shared/
│       ├── enums/                    # Định nghĩa các trạng thái (AuctionStatus, OrderStatus, UserRole...)
│       ├── model/                    # Thực thể lõi (User, Auction, Order, Notification, Transaction...)
│       ├── network/                  # Lớp cấu hình mạng (NetworkConfig) đọc từ config.properties
│       ├── request/                  # Định nghĩa các mẫu yêu cầu (Request DTOs) gửi từ Client
│       ├── response/                 # Định nghĩa các mẫu phản hồi (Response DTOs) gửi từ Server
│       └── util/                     # Các lớp tiện ích chung
│
├── Auction_server/                   # 🖥️ MODULE BACKEND (Server Module)
│   ├── pom.xml
│   └── src/main/java/
│       ├── config/                   # Cấu hình DatabaseConnection (HikariCP → TiDB Cloud)
│       ├── repository/               # Data Access Layer (UserRepo, AuctionRepo, WalletRepo...)
│       ├── scheduler/                # Background Schedulers (Auto-close auctions, Cancel expired orders)
│       ├── servercontroller/         # TCP Socket Server (Server, ClientHandler, AuctionRooms)
│       └── service/                  # Business Logic Layer & ImageHttpServer
│
├── Auction_client/                   # 🖼️ MODULE FRONTEND (Client Module)
│   ├── pom.xml
│   ├── config.properties             # Cấu hình kết nối Client → Server
│   └── src/main/
│       ├── resources/                # Giao diện .fxml, stylesheet .css, font chữ
│       └── java/com/auction/client/
│           ├── Main.java             # Điểm khởi chạy ứng dụng JavaFX
│           ├── network/              # Quản lý kết nối Socket (ServerConnection, ResponseHandler)
│           ├── service/              # Logic nghiệp vụ phía Client
│           └── screenhandler/        # Controllers quản lý màn hình GUI
│               └── admin/            # Controllers riêng cho phân hệ Admin
│
└── Auction_report/                   # 📄 MODULE BÁO CÁO (Report & Coverage)
    └── pom.xml                       # JaCoCo aggregate coverage report across all modules
```

---

## 📦 4. Vị trí các tệp `.jar` sau khi đóng gói

Sau khi build dự án thành công bằng lệnh Maven, các tệp đóng gói sẽ nằm tại thư mục `target` của mỗi module:

| Module | Đường dẫn tệp `.jar` | Mô tả |
|---|---|---|
| **Shared** | `Auction_shared/target/Auction_shared-1.0-SNAPSHOT.jar` | Thư viện dùng chung (Library JAR) |
| **Client** | `Auction_client/target/Auction_client-1.0-SNAPSHOT.jar` | Ứng dụng JavaFX Client (Standard JAR) |
| **Client (Fat JAR)** | `Auction_client/target/Auction_client-1.0-SNAPSHOT-fat.jar` | 🌟 **Khuyên dùng** — Executable Fat JAR (~18 MB) |
| **Server** | `Auction_server/target/Auction_server-1.0-SNAPSHOT.jar` | Ứng dụng Server (Standard JAR) |
| **Server (Fat JAR)** | `Auction_server/target/Auction_server-1.0-SNAPSHOT-jar-with-dependencies.jar` | 🌟 **Khuyên dùng** — Executable Fat JAR (~22 MB) |

---

## 🚀 5. Hướng dẫn khởi chạy chi tiết (Server & Client)

> [!IMPORTANT]
> Server của hệ thống hiện đang được **triển khai và vận hành 24/7 trên máy ảo Azure VM** (IP: `20.189.123.212`). Bạn **chỉ cần chạy Client** là ứng dụng sẽ tự động kết nối đến Server trên cloud — **không cần khởi chạy Server trên máy local**.

### 👥 5.1. Khởi chạy NHANH Client & Server bằng file `.jar` có sẵn (Khuyên dùng)

Để khởi chạy nhanh hệ thống mà không cần cài đặt các công cụ build hay biên dịch lại mã nguồn từ đầu, bạn có thể sử dụng gói chạy thử nghiệm được cung cấp sẵn trong phần Release của dự án:

1. **Truy cập phần Releases** trên kho lưu trữ GitHub của dự án.
2. **Tải file nén** `Dau-gia-88-release-latest.zip` về máy tính của bạn.
3. **Giải nén** file zip vừa tải. Sau khi giải nén, bạn sẽ có thư mục `Dau-gia-88-release-latest` chứa các tệp sau:
   - `Auction_client-1.0-SNAPSHOT-fat.jar` (File chạy ứng dụng Client).
   - `Auction_server-1.0-SNAPSHOT-jar-with-dependencies.jar` (File chạy ứng dụng Server).
   - `config.properties` (File cấu hình IP Server và Cổng kết nối).
4. **Mở Terminal/Command Prompt** và di chuyển vào đúng thư mục `Dau-gia-88-release-latest` vừa giải nén (ví dụ chạy lệnh `cd Dau-gia-88-release-latest` hoặc click chuột phải vào thư mục và chọn *Open in Terminal* / *Open PowerShell window here*).

Tùy theo nhu cầu sử dụng, bạn chọn một trong hai phương thức chạy sau đây:

#### 🌐 Cách A — Kết nối tới Server Online trên Cloud (Mặc định)
Hiện tại, file `config.properties` trong thư mục giải nén đang được cấu hình mặc định để kết nối trực tiếp đến Server Online chạy trên cloud (Azure VM).

Tại cửa sổ terminal đang mở trong thư mục giải nén, bạn chỉ cần chạy lệnh sau để mở Client:
```bash
java -jar Auction_client-1.0-SNAPSHOT-fat.jar
```
*Giao diện đăng nhập JavaFX sẽ hiển thị, bạn có thể thực hiện đăng ký tài khoản hoặc đăng nhập trực tiếp để bắt đầu đấu giá.*

#### 🖥️ Cách B — Kết nối tới Server Local (Tự vận hành Server trên máy cá nhân)
Nếu bạn muốn tự chạy cả Server và Client trên máy của mình:

1. **Chỉnh cấu hình**: Mở file `config.properties` bằng phần mềm soạn thảo văn bản (Notepad, VS Code,...) trong thư mục giải nén và thay đổi `server.host` về `localhost`:
   ```properties
   server.host=localhost
   ```
   Lưu file lại.
2. **Khởi chạy Server trước**: Tại terminal đang ở thư mục giải nén, chạy lệnh để bật Server:
   ```bash
   java -jar Auction_server-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```
   *Giữ nguyên terminal này chạy ngầm. Khi Server khởi động hoàn tất, nó sẽ lắng nghe cổng 8080 và 9090.*
3. **Khởi chạy Client**: Mở một cửa sổ terminal mới, di chuyển vào thư mục giải nén `Dau-gia-88-release-latest` và chạy lệnh:
   ```bash
   java -jar Auction_client-1.0-SNAPSHOT-fat.jar
   ```

---

### 🛠️ 5.2. Tùy chọn biên dịch & khởi chạy từ mã nguồn (Khi có chỉnh sửa code)
Nếu bạn thay đổi mã nguồn hoặc muốn tự build lại hệ thống từ đầu, hãy làm theo các bước sau:

> [!IMPORTANT]
> **YÊU CẦU BẮT BUỘC**: Tất cả các lệnh biên dịch và chạy bằng Maven (`mvnw` / `mvnw.cmd`) hoặc file `.jar` dưới đây phải được thực hiện khi terminal của bạn đang mở tại **đúng thư mục gốc của dự án** (thư mục `Auction_system` chứa file `pom.xml` của parent project và các file wrapper `mvnw` / `mvnw.cmd`). Tránh chạy lệnh từ các thư mục con hoặc thư mục khác để tránh lỗi thiếu file cấu hình hoặc sai đường dẫn.


#### 🔹 Bước 1: Biên dịch và đóng gói dự án
Mở terminal/dòng lệnh tại **thư mục gốc của dự án** (`Auction_system`) và chạy:
```bash
# Trên Windows (cmd/PowerShell):
.\mvnw.cmd clean package -DskipTests

# Trên macOS/Linux:
chmod +x mvnw
./mvnw clean package -DskipTests
```
*(Lệnh này sẽ dọn dẹp thư mục build cũ, biên dịch mã nguồn Java 21 mới nhất và sinh ra các tệp `.jar` mới trong thư mục `target` của các module).*

#### 🔹 Bước 2: Khởi chạy Client từ bản build mới
Tại **thư mục gốc của dự án**, bạn có thể mở nhiều Client cùng lúc để đấu giá thử nghiệm theo một trong các cách sau:

* **Cách A — Chạy qua file JAR vừa build (Đơn giản nhất):**
  ```bash
  java -jar Auction_client/target/Auction_client-1.0-SNAPSHOT-fat.jar
  ```
* **Cách B — Chạy thông qua Maven:**
  ```bash
  # Trên Windows (cmd/PowerShell):
  .\mvnw.cmd -pl Auction_client compile exec:java -Dexec.mainClass="com.auction.client.Main"

  # Trên macOS/Linux:
  ./mvnw -pl Auction_client compile exec:java -Dexec.mainClass="com.auction.client.Main"
  ```
* **Cách C — Chạy trên các IDE (IntelliJ IDEA / Eclipse):**
  1. Mở thư mục gốc của dự án bằng IDE.
  2. Chờ IDE đồng bộ và nạp cấu hình Maven.
  3. Mở lớp `com.auction.client.Main` tại module `Auction_client` và chọn **Run** (phím tắt `Shift + F10`).

> [!TIP]
> **Tài khoản Admin dùng thử hệ thống:**
> * **Đăng nhập (Sđt):** `12345678`
> * **Mật khẩu (Password):** `admin`

---

### 🖥️ (Tùy chọn) Chạy Server trên máy local

Nếu bạn muốn chạy Server riêng trên máy local thay vì sử dụng Server trên Azure VM, thực hiện theo các bước sau:

> [!IMPORTANT]
> **YÊU CẦU BẮT BUỘC**: Bạn phải mở terminal tại **đúng thư mục gốc của dự án** (`Auction_system`) để thực hiện các thao tác và chạy lệnh dưới đây. Điều này đảm bảo Server đọc đúng tệp `config.properties` nằm ở thư mục gốc để nạp cấu hình và tìm đúng các đường dẫn tương đối của các file build.

**Bước A — Chỉnh cấu hình Client trỏ về localhost:**

Mở file `config.properties` (nằm tại thư mục gốc của hệ thống) và thay đổi giá trị `server.host` từ IP Azure VM sang `localhost` (bạn nên thay đổi đồng thời cả file `Auction_client/config.properties` để chạy ứng dụng ổn định khi chọn nút Run trực tiếp trong IDE):

```properties
# Trước (kết nối Azure VM):
server.host=20.189.123.212

# Sau (kết nối Server local):
server.host=localhost
```

**Bước B — Khởi chạy Server local (BẮT BUỘC CHẠY TRƯỚC Client):**
Mở terminal tại **thư mục gốc của dự án** (`Auction_system`) và chạy một trong hai cách sau:

```bash
# Cách 1 — Chạy qua Fat JAR (✅ Khuyên dùng):
java -jar Auction_server/target/Auction_server-1.0-SNAPSHOT-jar-with-dependencies.jar

# Cách 2 — Chạy thông qua Maven:
# Trên Windows (cmd/PowerShell):
.\mvnw.cmd -pl Auction_server compile exec:java -Dexec.mainClass="servercontroller.Server"

# Trên macOS/Linux:
./mvnw -pl Auction_server compile exec:java -Dexec.mainClass="servercontroller.Server"
```

> [!TIP]
> Khi Server khởi động thành công, nó sẽ tự động chạy **hai dịch vụ song song**:
> 1. **TCP Socket Service** trên cổng `8080` — xử lý toàn bộ logic đấu giá.
> 2. **HTTP Image Server** trên cổng `9090` — phục vụ lưu trữ và phân phát ảnh sản phẩm.

> [!WARNING]
> Khi chạy Server local, hãy đảm bảo **khởi chạy Server trước rồi mới mở Client** để tránh lỗi kết nối socket. Sau khi hoàn tất, nhớ đổi `server.host` về IP Azure VM nếu muốn quay lại sử dụng Server trên cloud.

**Bước C — Khởi chạy Client** (như Bước 2 ở trên).

---

## ✅ 6. Danh sách các chức năng đã hoàn thành

Hệ thống đã được thiết kế và triển khai hoàn thiện các chức năng nghiệp vụ đấu giá chuyên nghiệp:

### 🔑 6.1. Xác thực & Quản lý Tài khoản (Authentication & Accounts)
- [x] **Đăng ký (Sign Up)**: Đăng ký tài khoản Bidder mới, kiểm tra trùng lặp email/username, validate định dạng trường thông tin.
- [x] **Đăng ký làm Người bán (Seller Register)**: Người dùng điền thông tin và gửi yêu cầu nâng cấp tài khoản lên Seller (được Admin phê duyệt).
- [x] **Đăng nhập bảo mật (Login)**: Xác thực đăng nhập qua mật khẩu băm BCrypt, hỗ trợ đăng nhập nhanh bằng phím `Enter`.
- [x] **Cơ chế Single Session**: Tự động phát hiện và ngăn chặn việc một tài khoản đăng nhập đồng thời trên nhiều thiết bị. Xử lý triệt để lỗi treo phiên khi Client đóng ứng dụng đột ngột.
- [x] **Hồ sơ cá nhân (Profile & Edit Profile)**: Xem và cập nhật các thông tin cá nhân bao gồm họ tên, số điện thoại, mật khẩu, và ảnh đại diện.

### 💰 6.2. Phân hệ Ví điện tử & Giao dịch (E-Wallet & Transactions)
- [x] **Ví điện tử tích hợp**: Hiển thị chi tiết số dư khả dụng (Available Balance) và số dư bị tạm đóng băng (Frozen Balance).
- [x] **Cơ chế Đóng băng số dư (Balance Freeze)**: Khi Bidder đặt giá cao nhất, hệ thống tạm đóng băng số tiền tương ứng. Khi bị người khác trả giá cao hơn, tiền tự động hoàn trả về số dư khả dụng ngay lập tức.
- [x] **Nạp tiền (Deposit) & Rút tiền (Withdraw)**: Lập yêu cầu giao dịch nạp/rút tiền và đưa vào danh sách chờ Admin phê duyệt.

### 🔨 6.3. Phân hệ Đấu giá cho Người mua (Bidder Features)
- [x] **Trang chủ hiện đại (Homepage)**: Hiển thị sản phẩm đang đấu giá trực quan với thanh tìm kiếm và phân loại sản phẩm theo danh mục.
- [x] **Phòng đấu giá thời gian thực (Auction Room)**:
    - Đồng hồ đếm ngược (Countdown Timer) trực quan: ngày, giờ, phút, giây.
    - Lịch sử đặt giá cập nhật realtime qua cơ chế **Broadcast Socket** cho mọi người dùng trong phòng.
- [x] **Đặt giá (Bidding)**: Xác thực mức giá (> giá cao nhất hiện tại + bước giá tối thiểu) và kiểm tra số dư khả dụng của ví. Hệ thống tự động đóng băng 10% giá trị bid làm tiền cọc.
- [x] **Đấu giá tự động (Auto-Bid)**: Cho phép Bidder cấu hình giá tối đa sẵn sàng trả và bước giá. Hệ thống sử dụng thuật toán **State-Machine loop** để tự động giải quyết cuộc đấu giá giữa các bot: xếp hạng theo giá tối đa, tính proxy price, kiểm tra số dư cọc, loại bỏ bot thua cuộc — tất cả chạy hoàn toàn trên RAM trước khi commit xuống DB.
- [x] **Chống đặt giá phút chót — Anti-Sniping**: Nếu có lượt đặt giá mới trong vòng **3 phút cuối**, phiên đấu giá tự động gia hạn thêm **3 phút**, áp dụng cho cả manual bid và auto-bid fight.

### 📦 6.4. Phân hệ Quản lý cho Người bán (Seller Features)
- [x] **Đăng tải đấu giá mới (Upload Item)**: Đăng sản phẩm kèm tên, giá khởi điểm, bước giá tối thiểu, thời gian kết thúc, ảnh mô tả và các **thuộc tính động (Dynamic Attributes)**.
- [x] **Xem chi tiết sản phẩm (Item View)**: Xem thông tin chi tiết và lịch sử các lượt đấu giá của sản phẩm đã đăng.
- [x] **Quản lý sản phẩm**: Theo dõi sản phẩm đang chờ duyệt, đang diễn ra đấu giá và đã hoàn thành.

### 🧾 6.5. Quản lý Đơn hàng & Xuất hóa đơn (Orders & PDF Invoices)
- [x] **Xác nhận giao dịch tự động**: Khi phiên đấu giá kết thúc có người thắng cuộc, hệ thống tự động khởi tạo đơn hàng trạng thái *Chờ xác nhận (Pending)*.
- [x] **Hóa đơn và Thanh toán**:
    - Người thắng cuộc thanh toán trực tiếp từ số dư ví điện tử.
    - Người bán và Người mua có thể xác nhận đơn hàng đã giao dịch thành công.
- [x] **Xuất hóa đơn PDF chuyên nghiệp**: Chuyển đổi HTML Template sang tệp `.pdf` sắc nét, cho phép tải xuống và lưu giữ hóa đơn chính thức.

### 🔔 6.6. Hệ thống Thông báo (Notification System)
- [x] **Thông báo thời gian thực**: Người dùng nhận thông báo ngay lập tức khi có sự kiện quan trọng — hệ thống bao phủ **17+ kịch bản** thông báo (đấu giá thắng/thua, đơn hàng, giao dịch ví, phê duyệt seller, hệ thống...).
- [x] **Toast Notification UI**: Popup thông báo hiện đại với hiệu ứng **slide-in/slide-out animation**, tự động ẩn sau 8 giây, hỗ trợ click callback và hover effects.
- [x] **Quản lý thông báo**: Xem danh sách tất cả thông báo với badge đếm chưa đọc, đánh dấu đã đọc từng mục hoặc tất cả.

### 🛡️ 6.7. Bảng điều khiển Quản trị viên (Admin Dashboard)
- [x] **Quản lý người dùng (User Manager)**: Danh sách toàn bộ thành viên, cho phép tạo thêm tài khoản admin mới.
- [x] **Phê duyệt người bán (Seller Account Manager)**: Phê duyệt/Từ chối yêu cầu nâng cấp lên tài khoản Người bán.
- [x] **Duyệt giao dịch tài chính (Pending Transaction Manager)**: Phê duyệt/Từ chối các giao dịch nạp tiền hoặc rút tiền từ ví.
- [x] **Quản lý phiên đấu giá (Auction Manager)**: Phê duyệt sản phẩm mới đăng để chính thức bắt đầu phiên đấu giá công khai.

### 🕒 6.8. Tiến trình Hệ thống tự động (Automated Schedulers)
- [x] **Auction Status Scheduler**: Tiến trình ngầm kiểm tra mỗi giây, quét các phiên đấu giá hết thời gian → xác định người thắng → khởi tạo đơn hàng, hoặc hủy phiên nếu không có ai đặt giá.
- [x] **Order Expiry Scheduler**: Tự động hủy đơn hàng quá hạn thanh toán 7 ngày (trạng thái Pending) và hoàn trả tiền cọc / áp dụng biện pháp kỷ luật.

### ⚡ 6.9. Tính năng kỹ thuật nâng cao
- [x] **Xử lý Concurrent Bidding**: Sử dụng `ConcurrentHashMap` + per-auction `synchronized` lock đảm bảo **xử lý tuần tự bid trong cùng phiên** nhưng **song song giữa các phiên khác nhau**. Kết hợp `SELECT ... FOR UPDATE` trên DB để lock row-level cho dữ liệu ví và đấu giá.
- [x] **Realtime Update qua Broadcast**: Áp dụng pattern **PostCommitEvents** — gom tất cả sự kiện broadcast trong transaction, chỉ gửi sau khi DB commit thành công. Hỗ trợ 3 loại broadcast: room-based, global, và per-user targeted.
- [x] **Xử lý lỗi & Graceful Shutdown**: Xử lý đầy đủ ngoại lệ mạng (mất kết nối đột ngột, timeout, lỗi Socket), tự động dọn dẹp phiên đăng nhập khi client ngắt kết nối. `synchronized (out)` trên output stream ngăn chặn interleaved writes.
- [x] **CI/CD với GitHub Actions**: 2 workflow — **CI** (build + test tự động với `xvfb-run` cho JavaFX headless, upload artifact, publish test results) và **CD** (auto deploy lên Azure VM qua SSH khi push vào branch `develop`).
- [x] **Unit Testing toàn diện**: 12 file test với **JUnit 5 + Mockito**, bao phủ toàn bộ Service layer và Scheduler. Sử dụng **Dependency Injection pattern** (dual constructors) cho test-injectable. **JaCoCo** aggregate coverage report qua module `Auction_report`.

---

## 📊 7. Báo cáo PDF

> [!NOTE]
> Báo cáo PDF (tối đa 5 trang) bao gồm các nội dung chính:
> - Giới thiệu mục tiêu và phạm vi thực hiện.
> - Kiến trúc tổng thể của hệ thống (Client-Server, Multi-module Maven).
> - Trình bày ngắn gọn các chức năng đạt được theo barem điểm, bao gồm chức năng, hướng giải quyết và lý do lựa chọn.

📎 **Link báo cáo PDF**: [Bao_cao.pdf](https://drive.google.com/file/d/1sDuMzdVgrKeVFjlJcNh1_0twbZgIk_-Y/view?usp=sharing)

---

## 🎥 8. Video Demo

> [!NOTE]
> Video demo (tối đa 3 phút) thể hiện quá trình chạy thực tế của hệ thống, bao gồm:
> - Cách khởi chạy Server và nhiều Client đồng thời.
> - Các chức năng chính: đăng ký, đăng nhập, ví điện tử, đăng sản phẩm, đấu giá, thanh toán, xuất PDF.
> - Tình huống kỹ thuật quan trọng: **concurrent bidding** (nhiều người đấu giá cùng lúc), **realtime update** (cập nhật giá tức thời qua broadcast), **auto-bid** (đấu giá tự động).

🎬 **Link video demo**: [Xem video trên Google Drive](https://drive.google.com/file/d/1Ifad1lTmD2B54grAJsOM-mA_GxcqfF64/view?usp=drive_link)

---

## 👥 Thành viên nhóm — Group 7 (CS6)

| STT | Họ và Tên | MSSV |
|:---:|---|:---:|
| 1 | Lê Kế Đức | 25021729 |
| 2 | Nguyễn Văn Đức | 25021736 |
| 3 | Nguyễn Minh Đạt | 25021713 |
| 4 | Nguyễn Tiến Hợi | 25021780 |

---

*Chúc các bạn có những trải nghiệm đấu giá tuyệt vời cùng **Online Auction System**! Mọi phản hồi hoặc báo cáo lỗi vui lòng tạo Issue trên kho lưu trữ của nhóm.* 🚀
