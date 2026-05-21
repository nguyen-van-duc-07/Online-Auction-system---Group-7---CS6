module com.auction.client {
    requires org.controlsfx.controls;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.auction.shared;
    requires static lombok;
    requires java.desktop;
    requires com.auction.server;
    requires openhtmltopdf.pdfbox; // Gọi sang Shared

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    // Cho phép JavaFX truy cập package gốc
    opens com.auction.client to javafx.fxml;
    exports com.auction.client;

    // Cấp quyền cho JavaFX truy cập và khởi tạo các Controller
    opens com.auction.client.screenhandler to javafx.fxml;
    exports com.auction.client.screenhandler;

    // Cấp quyền cho JavaFX truy cập và khởi tạo tại các Controller trong admin
    opens com.auction.client.screenhandler.admin to javafx.fxml;
    exports com.auction.client.screenhandler.admin;
}