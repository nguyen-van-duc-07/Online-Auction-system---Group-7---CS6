module com.auction.server {
    // Yêu cầu (requires) quyền truy cập vào module dùng chung
    requires com.auction.shared;
    requires jdk.httpserver;
    requires org.controlsfx.controls;
    requires javafx.controls;
    // Nếu bạn dùng các thư viện như BCrypt hay JDBC trong server, bạn cũng phải requires chúng ở đây
    requires java.sql; // Cần cho DatabaseConnection
    requires jbcrypt;
    requires static lombok;
    requires openhtmltopdf.pdfbox;  // Cần cho AuthService (bạn có thể phải check lại tên chính xác của module jbcrypt)
    requires com.zaxxer.hikari;
    requires com.google.gson;
    // Xuất các package của server nếu cần (tùy chọn)
    exports service;
    exports repository;
    exports servercontroller;
    exports config;
  exports scheduler;
}