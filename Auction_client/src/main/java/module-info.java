module com.auction.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.auction.shared;
    requires static lombok; // Gọi sang Shared

    // Cho phép JavaFX truy cập package gốc
    opens com.auction.client to javafx.fxml;
    exports com.auction.client;

    // Cấp quyền cho JavaFX truy cập và khởi tạo các Controller
    opens com.auction.client.screenhandler to javafx.fxml;
    exports com.auction.client.screenhandler;
}