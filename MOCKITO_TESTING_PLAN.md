# Ke hoach ap dung Mockito cho Auction System

## 1. Muc tieu

Muc tieu cua ke hoach nay la dua Mockito vao he thong theo cach phu hop voi cau truc hien tai cua du an:

- Giam phu thuoc vao MySQL/TiDB, socket, JavaFX UI khi viet unit test.
- Tang test coverage cho cac luong nghiep vu quan trong o `Auction_server`.
- Giu cac test hien co trong `Auction_shared` theo huong JUnit thuan vi phan nay chu yeu la model, DTO, enum va util.
- Refactor tung buoc, tranh thay doi lon lam anh huong luong chay hien tai.

Ket luan tong quan: Mockito phu hop nhat voi `Auction_server`, phu hop mot phan voi `Auction_client`, va khong can dung nhieu trong `Auction_shared`.

## 2. Hien trang du an

Du an la Maven multi-module:

- `Auction_shared`: chua model, enum, request/response DTO, util.
- `Auction_client`: JavaFX client, xu ly UI, socket client, session va response handler.
- `Auction_server`: service nghiep vu, repository JDBC, socket server, scheduler.
- `Auction_report`: aggregate Jacoco report.

Hien da co:

- JUnit 5 trong `Auction_shared` va `Auction_client`.
- JaCoCo trong parent `pom.xml`.
- Nhieu test cho model/util/session/invoice.

Hien chua co:

- Mockito dependency.
- Test nghiep vu day du cho `Auction_server`.
- Constructor injection cho service/repository, nen viec mock dependency con kho.

## 3. Nguyen tac ap dung

1. Khong mock cac model don gian trong `Auction_shared`.
2. Uu tien mock cac dependency cham hoac kho kiem soat: database, socket, scheduler, UI thread, static gateway.
3. Moi service quan trong nen co constructor phuc vu test, nhung van giu constructor mac dinh cho production.
4. Khong dua Mockito vao de che giau thiet ke kho test. Neu mot class phai mock qua static/reflection qua nhieu, can refactor nhe truoc.
5. Test nghiep vu nen kiem tra ca ket qua tra ve va interaction quan trong voi repository/service khac.

## 4. Giai doan 1: Chuan hoa cau hinh test

### 4.1. Cap nhat parent `pom.xml`

Them version chung:

```xml
<mockito.version>5.12.0</mockito.version>
```

Nen dung chung `junit.version` da co trong parent thay vi moi module khai bao version rieng le.

### 4.2. Them Mockito cho module can dung

Them vao `Auction_server/pom.xml`:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>${junit.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>${junit.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>${mockito.version}</version>
    <scope>test</scope>
</dependency>
```

Them vao `Auction_client/pom.xml` neu bat dau test `ResponseHandler`, controller hoac network:

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>${mockito.version}</version>
    <scope>test</scope>
</dependency>
```

### 4.3. Kiem tra JPMS/module-info

Du an dang dung `module-info.java`. Neu Mockito gap loi reflection khi chay test, can them cau hinh Surefire `--add-opens` cho cac package server can test, tuong tu cach `Auction_client` da lam.

Ung vien can mo:

- `com.auction.server/service`
- `com.auction.server/repository`
- `com.auction.server/servercontroller`
- `com.auction.server/scheduler`

## 5. Giai doan 2: Refactor nhe de test duoc bang Mockito

### 5.1. Mau refactor chung cho service

Hien tai nhieu service dang tu tao dependency:

```java
private final AuctionRepository auctionRepo = new AuctionRepository();
```

Nen doi thanh:

```java
private final AuctionRepository auctionRepo;

public SomeService() {
    this(new AuctionRepository());
}

SomeService(AuctionRepository auctionRepo) {
    this.auctionRepo = auctionRepo;
}
```

Constructor test co the package-private de khong mo API qua rong.

### 5.2. Thu tu refactor uu tien

1. `WalletService`
2. `OrderService`
3. `BidService`
4. `NotificationService`
5. `AutoBidService`
6. `AuctionStatusScheduler` va `OrderExpiryScheduler`
7. `RequestHandler`
8. `AuctionService`

Ly do: bat dau tu service co boundary ro hon (`WalletService`, `OrderService`) truoc khi den `BidService`, vi `BidService` co nhieu logic transaction, auto-bid va broadcast hon.

### 5.3. Xu ly `DatabaseConnection.getConnection()`

Day la static dependency kho test. Co hai huong:

Huong khuyen nghi:

- Tao interface/adapter nho, vi du `ConnectionProvider`.
- Production implementation goi `DatabaseConnection.getConnection()`.
- Test mock `ConnectionProvider`.

Vi du:

```java
public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}
```

Huong tam thoi:

- Giu cac method repository nhan `Connection conn` nhu hien co.
- Test service bang mock `Connection`, `PreparedStatement`, `ResultSet` chi khi that su can.
- Tranh static mocking neu co the refactor injection duoc.

## 6. Giai doan 3: Viet unit test uu tien cho server

### 6.1. `WalletServiceTest`

Muc tieu: test logic vi tien ma khong can database that.

Test cases uu tien:

- Lay balance thanh cong.
- Nap tien tao transaction pending.
- Rut tien khi du so du.
- Rut tien khi thieu so du.
- Freeze tien thanh cong khi du balance.
- Freeze tien that bai khi khong du balance.
- Release frozen balance dung auction id.
- Process transaction approve.
- Process transaction reject.
- Khi repository nem exception thi service tra response that bai va khong goi side effect tiep theo.

Mockito can dung:

- `@Mock WalletRepository`
- `@Mock WalletTransactionRepository`
- `@Mock SellerProfileRepository`
- `@Mock NotificationService`
- `@InjectMocks` neu constructor injection da san sang

### 6.2. `OrderServiceTest`

Test cases uu tien:

- Xac nhan don hang thanh cong.
- Huy don hang thanh cong.
- Khong cho xac nhan don khong o trang thai pending.
- Khong cho huy don da confirmed.
- Khi order khong ton tai thi tra response that bai.
- Xac nhan don goi dung wallet transfer/update seller profile/notification.
- Loi repository thi rollback va response that bai.

Mockito can verify:

- `orderRepo.find...`
- `orderRepo.update...`
- `walletService...`
- `notifService...`

### 6.3. `BidServiceTest`

Day la nhom test quan trong nhat, nen lam sau khi da refactor dependency.

Test cases cho manual bid:

- Auction khong ton tai.
- Auction khong ACTIVE.
- Auction da het thoi gian.
- Seller tu bid san pham cua minh.
- Bid nho hon `currentHighestPrice + minStepPrice`.
- Bid hop le thi:
  - release coc nguoi dang dan dau cu neu co
  - freeze coc nguoi moi
  - luu bid transaction
  - cap nhat gia auction
  - tra response thanh cong

Test cases cho auto-bid:

- Chi co mot bot va bot dang la highest bidder thi khong tao bid moi.
- Bot top co du tien thi dat gia proxy hop le.
- Bot top thieu tien thi bi deactivate.
- Nhieu bot trung max price thi xu ly dung theo rule hien tai.
- Anti-sniping chi gia han khi con it hon threshold.
- Khong broadcast truoc khi commit thanh cong.

Luu y: phan broadcast qua `Server.broadcastToAll`, `Server.broadcastToAuctionRoom`, `Server.sendToUser` dang la static. Nen tach thanh `AuctionEventPublisher` de mock sach hon.

### 6.4. `AuthServiceTest`

Test cases uu tien:

- Login dung username/password.
- Login sai password.
- Login user khong ton tai.
- Signup thanh cong tao user va wallet.
- Signup trung username/email.
- Change password thanh cong.
- Change password sai mat khau cu.
- Update profile thanh cong.

Luu y: `BCrypt` co the dung that trong test neu khong qua cham. Repository nen mock.

### 6.5. `NotificationServiceTest`

Test cases uu tien:

- Tao notification dung user/type/title/content.
- Mark read thanh cong.
- Get notifications tra dung danh sach.
- Gui new auction notification cho dung nhom user.

Mockito verify:

- `notifRepo.save(...)`
- `notifRepo.markAsRead(...)`
- `Server.sendToUser(...)` neu chua tach publisher.

## 7. Giai doan 4: Test scheduler va request handler

### 7.1. `AuctionStatusSchedulerTest`

Nen tach logic `updateAuctionStatus()` thanh package-private hoac mot class worker rieng de test truc tiep, khong can doi scheduler that.

Test cases:

- Tim auction can activate va goi activate.
- Tim auction can close va goi close.
- Dong auction co highest bidder thi tao order/notification.
- Auction active het han nhung da duoc anti-sniping gia han thi khong dong sai.

### 7.2. `OrderExpirySchedulerTest`

Test cases:

- Don qua han bi huy.
- Don chua qua han khong bi huy.
- Khi service nem exception scheduler khong dung toan bo.

### 7.3. `RequestHandlerTest`

Hien `RequestHandler` co nhieu static method va tu `new Service`, vi vay nen refactor sau.

Huong de xuat:

- Tao `RequestDispatcher` instance-based.
- `RequestHandler` static chi la wrapper goi sang dispatcher mac dinh.
- Test `RequestDispatcher` bang Mockito.

Test cases:

- Login request goi `AuthService.login`.
- Place bid request goi `BidService.placeBid`.
- Get balance request goi `WalletService.getBalance`.
- Update auction status request goi `AuctionService.updateAuctionStatusByAdmin`.

## 8. Giai doan 5: Ap dung co chon loc cho client

### 8.1. Nhung phan nen test bang Mockito

- Controller co validation input truoc khi gui request.
- `ResponseHandler` neu tach duoc UI navigation va network sending thanh dependency.
- `ServerConnection.sendData` neu tach `SocketClient`/`ConnectionState`.

### 8.2. Nhung phan khong nen bat dau bang Mockito

- JavaFX controller phu thuoc nhieu FXML khi chua co TestFX.
- `Platform.runLater` neu chua co utility de chay synchronous trong test.
- Socket real connection.

### 8.3. Refactor nhe cho client

Nen tao cac interface nho:

```java
public interface RequestSender {
    void send(Object request);
}
```

```java
public interface UiNavigator {
    void switchScreen(String fxml, String title);
    void showAlert(Alert.AlertType type, String title, String message);
}
```

Sau do controller/handler nhan dependency nay, test co the verify request gui di ma khong can server that.

## 9. Giai doan 6: Quy uoc viet test

### 9.1. Cau truc test

Dat ten test theo dang:

```java
methodName_condition_expectedResult()
```

Vi du:

```java
placeBid_bidBelowMinimum_returnsFailure()
confirmOrder_pendingOrder_updatesOrderAndWallet()
freezeMoney_insufficientBalance_returnsFalse()
```

### 9.2. Bo cuc test

Dung Arrange - Act - Assert:

```java
// Arrange
when(auctionRepo.findAuctionForUpdate(any(), eq("AUC_1"))).thenReturn(auction);

// Act
PlaceBidResponseDTO response = bidService.placeBid(request);

// Assert
assertFalse(response.isSuccess());
verify(bidRepo, never()).saveBid(any(), any());
```

### 9.3. Khong nen lam

- Khong mock class model don gian chi de set/get.
- Khong verify moi dong code noi bo, chi verify side effect co y nghia nghiep vu.
- Khong viet test phu thuoc database cloud that trong unit test.
- Khong dung reflection de set private field neu co the them constructor injection.
- Khong de unit test tao file, socket, thread scheduler that neu khong can.

## 10. Danh sach file uu tien can tao

Server:

- `Auction_server/src/test/java/service/WalletServiceTest.java`
- `Auction_server/src/test/java/service/OrderServiceTest.java`
- `Auction_server/src/test/java/service/BidServiceTest.java`
- `Auction_server/src/test/java/service/AuthServiceTest.java`
- `Auction_server/src/test/java/service/NotificationServiceTest.java`
- `Auction_server/src/test/java/scheduler/AuctionStatusSchedulerTest.java`
- `Auction_server/src/test/java/scheduler/OrderExpirySchedulerTest.java`
- `Auction_server/src/test/java/servercontroller/RequestDispatcherTest.java`

Client:

- `Auction_client/src/test/java/com/auction/client/network/ResponseHandlerTest.java`
- `Auction_client/src/test/java/com/auction/client/screenhandler/UploadItemControllerTest.java`
- `Auction_client/src/test/java/com/auction/client/screenhandler/ItemAuctionControllerTest.java`

Shared:

- Tiep tuc bo sung JUnit thuan khi co model/util moi.

## 11. Tieu chi hoan thanh theo tung moc

### Moc 1: Cau hinh

- `mvn test` chay duoc tren toan bo project.
- `Auction_server` co the chay JUnit 5 test.
- Mockito dependency duoc khai bao dung scope `test`.

### Moc 2: Refactor nen tang

- `WalletService`, `OrderService`, `BidService` co constructor injection.
- Production behavior khong doi vi van co constructor mac dinh.
- Khong can dung reflection de thay repository trong test.

### Moc 3: Unit test server cot loi

- Co test cho wallet, order, bid.
- Cac test khong can database cloud.
- Cac nhanh loi nghiep vu chinh deu duoc cover.

### Moc 4: Event/socket boundary

- Broadcast static duoc boc qua publisher/interface.
- Test co the verify event duoc publish ma khong mo socket.

### Moc 5: Bao cao coverage

- JaCoCo aggregate report tao duoc qua `mvn verify`.
- Tang coverage co y nghia o `Auction_server/service`.
- Khong chay theo coverage bang cach test getter/setter vo nghia.

## 12. Thu tu trien khai de xuat trong 1 sprint

1. Them Mockito va JUnit cho `Auction_server`.
2. Tao test smoke dau tien `WalletServiceTest`.
3. Refactor constructor injection cho `WalletService`.
4. Viet 5-8 test cho `WalletService`.
5. Refactor constructor injection cho `OrderService`.
6. Viet 5-8 test cho `OrderService`.
7. Refactor constructor injection cho `BidService`.
8. Viet cac test failure path truoc cho `placeBid`.
9. Viet test success path cho `placeBid`.
10. Chay `mvn test` va cap nhat cac loi module/JPMS neu co.
11. Chay `mvn verify` de kiem tra JaCoCo.
12. Ghi lai cac class con kho test vao backlog refactor tiep theo.

## 13. Backlog refactor sau khi Mockito hoat dong

- Tach `Server` static broadcast thanh `AuctionEventPublisher`.
- Tach `DatabaseConnection.getConnection()` thanh `ConnectionProvider`.
- Giam static method trong `AuthService`, `AuctionService`, `RequestHandler`.
- Tach `ResponseHandler` thanh phan xu ly state va phan dieu huong UI.
- Tach `ServerConnection` thanh socket transport va response dispatch.
- Them integration test rieng cho repository voi database local/test container neu can, khong tron voi unit test Mockito.

## 14. Ket luan

Mockito nen duoc ap dung vao du an, nhung khong nen dua vao mot cach dai tra ngay lap tuc. Gia tri lon nhat nam o viec test nghiep vu server ma khong can database that va khong can socket that. De dat duoc dieu do, nhom nen refactor nhe theo constructor injection, them boundary cho database va broadcast, roi viet test theo thu tu `WalletService` -> `OrderService` -> `BidService`.

